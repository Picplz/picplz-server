package com.hm.picplz.domain.area.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

	static final double EARTH_RADIUS = 111;

	/**
	 * 사용자의 현재 위치 근처의 법정동을 반환합니다.
	 * @param radius km 단위의 범위
	 * @param lat 위도
	 * @param lng 경도
	 * @return 근처 법정동 최대 10개
	 */
	public List<AreaDto.AreaInfo> getNearbyAreas(int radius, double lat, double lng) {
		if (lng == 0 || lat == 0 || radius == 0) {
			throw ExceptionFactory.of(AreaErrorCode.BAD_POSITION);
		}

		double latDiff = radius / EARTH_RADIUS;
		double lngDiff = radius / (EARTH_RADIUS * Math.cos(Math.toRadians(lat)));

		List<Area> nearAreas = areaRepository.findAreaInMBR(
			lat-latDiff, lng-lngDiff, lat+latDiff, lng+lngDiff
		);

		return nearAreas.stream()
			.map(AreaDto.AreaInfo::from)
			.toList();
	}

	/**
	 * 법정동을 키워드로 검색합니다.
	 * @param keyword 검색 키워드
	 * @return 해당 키워드를 포함하는 법정동 최대 10개
	 */
	public List<AreaDto.AreaInfo> searchAreasWithKeyword(String keyword) {
		return areaRepository.searchByKeyword(keyword).stream()
			.map(AreaDto.AreaInfo::from)
			.toList();
	}

	/**
	 * 모든 법정동을 조회합니다.
	 * 임시) 서울, 경기, 인천, 부산, 제주만 검색
	 * @return 모든 법정동 데이터
	 */
	public List<AreaDto.AllAreaInfo> getAllAreas() {
		List<AllAreaInfoProjection> result = areaRepository.findAllEupmyeondong();

		return result.stream()
				.collect(Collectors.groupingBy(AllAreaInfoProjection::getSido))  // 지역별로 묶음
				.entrySet().stream()
				.map(regionEntry -> {
					String region = regionEntry.getKey(); // ex) 서울, 경기 등
					Map<String, List<AllAreaInfoProjection>> districtGrouped = regionEntry.getValue().stream()
							.collect(Collectors.groupingBy(AllAreaInfoProjection::getSigungu)); // 구/시로 묶기

					List<AreaDto.DistrictDto> districts = districtGrouped.entrySet().stream()
							.map(districtEntry -> new AreaDto.DistrictDto(
									districtEntry.getKey(),
									districtEntry.getValue().stream()
											.map(AllAreaInfoProjection::getEupmyeondong)
											.distinct()
											.toList()
							))
							.toList();

					return new AreaDto.AllAreaInfo(region, districts);
				})
				.toList();
	}
}
