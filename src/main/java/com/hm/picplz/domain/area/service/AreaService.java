package com.hm.picplz.domain.area.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hm.picplz.domain.area.dto.AllAreaInfoProjection;
import org.springframework.stereotype.Service;

import com.hm.picplz.domain.area.domain.Area;
import com.hm.picplz.domain.area.dto.AreaDto;
import com.hm.picplz.domain.area.exception.AreaErrorCode;
import com.hm.picplz.domain.area.repository.AreaRepository;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AreaService {

	private final AreaRepository areaRepository;
	private final ObjectMapper objectMapper;

	static final double EARTH_RADIUS = 111;

	/**
	 * 사용자의 현재 위치 근처의 법정동을 반환합니다.
	 *
	 * @param radius km 단위의 범위
	 * @param lat    위도
	 * @param lng    경도
	 * @return 근처 법정동 최대 10개
	 */
	public List<AreaDto.AreaInfo> getNearbyAreas(int radius, double lat, double lng) {
		if (lng == 0 || lat == 0 || radius == 0) {
			throw ExceptionFactory.of(AreaErrorCode.BAD_POSITION);
		}

		double latDiff = radius / EARTH_RADIUS;
		double lngDiff = radius / (EARTH_RADIUS * Math.cos(Math.toRadians(lat)));

		List<Area> nearAreas = areaRepository.findAreaInMBR(
				lat - latDiff, lng - lngDiff, lat + latDiff, lng + lngDiff
		);

		return nearAreas.stream()
				.map(AreaDto.AreaInfo::from)
				.toList();
	}

	/**
	 * 법정동을 키워드로 검색합니다.
	 *
	 * @param keyword 검색 키워드
	 * @return 해당 키워드를 포함하는 법정동 최대 10개
	 */
	public List<AreaDto.AreaInfo> searchAreasWithKeyword(String keyword) {
		return areaRepository.searchByKeyword(keyword).stream()
				.map(AreaDto.AreaInfo::from)
				.toList();
	}

	/**
	 * 서울, 경기, 인천, 부산, 제주의 법정동을 조회합니다.
	 * @return 모든 법정동 데이터
	 */
	//TODO: 캐시 조회로 변경
	public List<AreaDto.AllAreaInfo> getAllAreas() {
		List<AllAreaInfoProjection> result = areaRepository.findAllEupmyeondong();
		Map<String, List<AreaDto.DistrictDto>> tmpMap = new HashMap<>(); // region : List<districtDto> 매핑
		// region : 서울, 부산 .. , districts : name, neighborhoods , districtDto.name : 강남구 .. , districtDto : 논현동 ..

		for (AllAreaInfoProjection item : result) { // 서울 / 강남구 / 수서동, 개포동, 도곡동 ..
			List<String> neighborhoods;
			try {
				neighborhoods = new ArrayList<>(objectMapper.readValue(
                        item.getNeighborhoods(), new TypeReference<List<String>>() {
                        })); // 해당 시군구의 모든 동 담기
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}

			// 하나의 시군구에 모든 동 담기
			AreaDto.DistrictDto district = new AreaDto.DistrictDto(item.getSigungu(), neighborhoods);
			// 임시 맵에 시도:시군구-동 리스트 삽입
			tmpMap.computeIfAbsent(item.getSido(), k -> new ArrayList<>()).add(district);
		}

		List<AreaDto.AllAreaInfo> response = new ArrayList<>();
		for (String region : tmpMap.keySet()) { // 맵으로 담아뒀던 거 List롤 변환하여 반환
			response.add(new AreaDto.AllAreaInfo(region, tmpMap.get(region)));
		}

		return response;
	}
}
