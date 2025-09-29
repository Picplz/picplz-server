package com.hm.picplz.domain.photographer.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.global.common.service.WebClientService;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.area.domain.Area;
import com.hm.picplz.domain.area.exception.AreaErrorCode;
import com.hm.picplz.domain.following.repository.FollowingRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.dto.MemberDto;
import com.hm.picplz.domain.member.service.MemberService;
import com.hm.picplz.domain.photographer.domain.ActiveArea;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.domain.PhotographerCamera;
import com.hm.picplz.domain.photographer.dto.PhotoMoodDto;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.ActiveAreaRepository;
import com.hm.picplz.domain.area.repository.AreaRepository;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.global.common.entity.YesNo;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

import static com.hm.picplz.domain.member.service.MemberService.GEO_KEY;

@Service
@RequiredArgsConstructor
public class PhotographerService {

	private final MemberService memberService;
	private final PhotoMoodService photoMoodService;
	private final WebClientService webClientService;
	private final PhotographerRepository photographerRepository;
	private final FollowingRepository followingRepository;
	private final ActiveAreaRepository activeAreaRepository;
	private final AreaRepository areaRepository;

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String PHOTOGRAPHER_REDIS_KEY = "photographer:";

	/**
	 * 작가 회원가입 메서드
	 * @param createPhotographerRequest 작가 회원가입을 위한 입력 정보
	 */
	@Transactional
	public PhotographerDto.Detail createPhotographer(PhotographerDto.CreatePhotographerRequest createPhotographerRequest) {
		// 마지막 닉네임 중복 확인
		memberService.checkNickname(createPhotographerRequest.getNickname());
		// 멤버 데이터 생성
		Member member = memberService.createMember(MemberDto.CreateMemberRequest.of(createPhotographerRequest,
			Role.PHOTOGRAPHER));

		// 작가 정보 생성
		Photographer photographer = Photographer.from(member);
		photographerRepository.save(photographer);

		// 작가 분위기 키워드
		createPhotoMoods(createPhotographerRequest.getPhotoMoods(), photographer);
		// 작가 주 촬영지
		createActiveAreas(createPhotographerRequest.getActiveAreas(), photographer);
		// 작가 촬영 카메라
		createPhotographerCameras(createPhotographerRequest.getCameras(), photographer);

		return PhotographerDto.Detail.of(photographer);
	}

	/**
	 * 작가 상세 정보 반환
	 * @param photographerId 조회하려는 작가 정보
	 * @param memberId 조회를 시도하는 회원 정보(팔로우 여부를 위해)
	 * @return 작가 상세 정보
	 */
	public PhotographerDto.Detail getPhotographerDetail(Long photographerId, Long memberId) {
		Photographer photographer = getPhotographerByPhotographerId(photographerId);
		int followersCount = getFollowers(photographer);
		YesNo isFollowing = isFollowing(photographer, memberId);
		return PhotographerDto.Detail.of(photographer, followersCount, isFollowing);
	}

	/**
	 * 작가 권한 확인을 위해 캐시 확인
	 * @param memberId 작가 권환 확인을 요청한 사용자의 pk
	 * @return 작가의 권한 확인 결과가 redis에 존재하는지
	 */
	public Boolean getCachedPhotographerExistence(Long memberId) {
		return (Boolean) redisTemplate.opsForValue().get(PHOTOGRAPHER_REDIS_KEY + memberId);
	}

	/**
	 * 작가인 경우, redis에 캐시로 저장해 작가 권한을 빠르게 확인하도록 한다.
	 * @param memberId 작가 권환 확인을 요청한 사용자의 pk
	 * @return 작가인지 아닌지 반환 (작가라면 redis에 캐싱)
	 */
	public boolean cachePhotographerExistence(Long memberId) {
		// 1. 픽플즈에 존재하는 회원인가?
		memberService.getMemberById(memberId);
		// 2. 작가인가?
		boolean exists = photographerRepository.existsByMemberId(memberId);
		redisTemplate.opsForValue().set(PHOTOGRAPHER_REDIS_KEY + memberId, exists, Duration.ofMinutes(30));
		return exists;
	}

	/**
	 * member Id로 작가인지 확인하고, 캐시가 없다면 작가 여부를 MySQL에서 확인하고 나서 반환한다.
	 * @param memberId 작가 권환 확인을 요청한 사용자의 pk
	 * @return 작가인가 아닌가
	 */
	public boolean checkAndCachePhotographer(Long memberId) {
		Boolean cached = getCachedPhotographerExistence(memberId);
		if (cached != null) {
			return cached;
		}
		return cachePhotographerExistence(memberId);
	}

	/**
	 * 작가의 분위기 키워드 추가
	 * @param addPhotoMoodDto 분위기 키워드 1개
	 * @param memberId 작가 member Id(토큰에서 파싱)
	 */
	@Transactional
	public void addPhotoMood(PhotoMoodDto.PhotoMoodRequest addPhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.addPhotoMood(addPhotoMoodDto.getPhotoMood(), photographer);
	}

	/**
	 * 작가의 분위기 키워드 삭제
	 * (삭제하려는 분위기의 pk를 받아도 될 것 같긴 합니다. 그러려면 분위기 조회 응답값을 바꿔야합니다.)
	 * @param deletePhotoMoodDto 분위기 키워드 1개
	 * @param memberId 작가 member Id(토큰에서 파싱)
	 */
	@Transactional
	public void deletePhotoMood(PhotoMoodDto.PhotoMoodRequest deletePhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.deletePhotoMood(deletePhotoMoodDto.getPhotoMood(), photographer);
	}

	/**
	 * 바로촬영 가능한 주변 작가 = 2km 반경 내 바로촬영 활성화 + 주 활동지역 = 현재 고객 위치인 작가
	 * 바로 촬영 작가 없을 시 주 활동지역 같은 작가 조회
	 * @param memberId 조회 기준이 되는 멤버 아이디
	 * @param distance 반경 (단위: km)
	 * @return 검색된 데이터 목록
	 */
	public List<PhotographerDto.Detail> findPhotographersWithinRadius(Long memberId, long distance) {

		/*
		 * 1. 멤버의 현재 위치 조회하여 해당 위치 기준 반경 distance(km -> m) 만큼 떨어져있는 멤버 조회
		 */
		Point currentPoint = memberService.getMemberLocation(memberId);
		Circle circle = new Circle(currentPoint, distance * 1000d);    // 반경 (단위: m)

		// 검색 옵션 (거리 포함, 가까운 순)
		RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
				.newGeoRadiusArgs()
				.includeDistance()
				.sortAscending();

		GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults = redisTemplate
				.opsForGeo()
				.radius(GEO_KEY, circle, args);

		List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> results =
				geoResults != null ? geoResults.getContent() : Collections.emptyList();

		/*
		 * 2. 조회한 redis 멤버 데이터를 가지고 바로 촬영 작가인지 확인 및 dto 화
		 */
		Long areaId = webClientService.requestAreaIdByPoint(currentPoint);
		List<PhotographerDto.Detail> list = getPhotographerDetailByMemberGeoInfo(results, memberId, areaId);

		/*
		* 현재 위치 반경으로 활동중(바로 촬영 가능)한 작가가 없을 시, 현재 고객 위치 = 주 활동지역 위치 작가 리스트 반환
		*/
		if(list.isEmpty()) {
			list.addAll(getPhotographersByActiveArea(memberId));
		}

		return list;
	}

	/**
	 * 현재 멤버의 위치 = 주 활동지역 작가 탐색
	 * @param memberId 조회를 진행한 멤버
	 * @return 작가 상세 정보
	 */
	public List<PhotographerDto.Detail> getPhotographersByActiveArea(Long memberId) {
		Long areaId = webClientService.requestAreaIdByPoint(
				memberService.getMemberLocation(memberId)
		);

		return activeAreaRepository.findAllByAreaIdWithPhotographer(areaId).stream()
				.map(ActiveArea::getPhotographer)
				.map(photographer -> {
					int followersCount = getFollowers(photographer);
					YesNo isFollowing = isFollowing(photographer, memberId);
					return PhotographerDto.Detail.of(photographer, followersCount, isFollowing);
				})
				.toList();
	}

	/**
	 * 작가의 주 활동지역 변경(교체)
	 * @param memberId 변경이 필요한 멤버
	 * @param updateActiveAreaRequestDto 바꾸고자 하는 주 활동지역
	 * @return 변경된 주 활동지역
	 */
	@Transactional
	public PhotographerDto.UpdateActiveAreaResponse updateActiveArea(Long memberId, PhotographerDto.UpdateActiveAreaRequest updateActiveAreaRequestDto) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photographer.removeAllActiveArea();
		createActiveAreas(updateActiveAreaRequestDto.getAreas(), photographer);
		return PhotographerDto.UpdateActiveAreaResponse.from(photographer);
	}

	/**
	 * 작가 enitty 데이터 조회 메서드
	 * @param memberId 작가의 member pk
	 * @return 작가 엔티티
	 */
	public Photographer getPhotographerByMemberId(Long memberId) {
		return photographerRepository.findByMemberId(memberId)
				.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	/**
	 * 작가의 분위기 키워드 목록 받아 1:N 관계 테이블 저장
	 * @param photoMoodContents 분위기 키워드
	 * @param photographer 작가
	 */
	private void createPhotoMoods(List<String> photoMoodContents, Photographer photographer) {
		// 1. null·빈 문자열 제거 & trim 후 중복 제거
		List<String> distinctContents = photoMoodContents.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.distinct()
				.toList();

		// 2. 엔티티로 매핑
		List<PhotoMood> photoMoods = distinctContents.stream()
				.map(content -> PhotoMood.builder()
						.photographer(photographer)
						.content(content)
						.build())
				.toList();

		photographer.addAllPhotoMoods(photoMoods);
	}

	/**
	 * 작가의 주 촬영지(법정동 데이터 기반) 목록을 받아 N:M 관계 테이블에 저장
	 * @param areaDtos 주 촬영지 목록
	 * @param photographer 작가
	 */
	private void createActiveAreas(List<PhotographerDto.ActiveAreaRequest> areaDtos, Photographer photographer) {

		List<ActiveArea> activeAreas = new ArrayList<>();

		for (PhotographerDto.ActiveAreaRequest dto : areaDtos) {
			Area area = areaRepository.findById(dto.getCode())
					.orElseThrow(() -> ExceptionFactory.of(AreaErrorCode.WRONG_AREA_CODE));

			ActiveArea activeArea = ActiveArea.builder()
					.photographer(photographer)
					.area(area)
					.priority(dto.getPriority())
					.build();

			activeAreas.add(activeArea);
		}
		photographer.addAllActiveAreas(activeAreas);
	}

	/**
	 * 작가의 촬영 기기 1:N 테이블 저장
	 * @param cameraDtos 작가의 촬영 기기
	 * @param photographer 작가
	 */
	private void createPhotographerCameras(List<PhotographerDto.PhotographerCameraRequest> cameraDtos, Photographer photographer) {
		List<PhotographerCamera> cameras = new ArrayList<>();

		for (PhotographerDto.PhotographerCameraRequest dto : cameraDtos) {
			PhotographerCamera camera = PhotographerCamera.builder()
					.photographer(photographer)
					.type(dto.getType())
					.brand(dto.getBrand())
					.name(dto.getName())
					.cameraType(dto.getCameraType())
					.build();
			cameras.add(camera);
		}

		photographer.addAllCameras(cameras);
	}

	/**
	 * 작가 entity 데이터 조회 메서드
	 * @param photographerId 작가 pk
	 * @return 작가 엔티티
	 */
	private Photographer getPhotographerByPhotographerId(Long photographerId) {
		return photographerRepository.findPhotographerWithMoods(photographerId)
				.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	/**
	 * 작가의 팔로워 수를 조회
	 * @param photographer 조회하려는 작가
	 * @return 작가의 팔로워 수
	 */
	private int getFollowers(Photographer photographer) {
		return Math.toIntExact(followingRepository.countByFollowingId(photographer.getMember().getId()));
	}

	/**
	 * 작가를 팔로우하고 있는지 확인
	 * @param photographer 작가
	 * @param memberId 로그인한 회원 pk
	 * @return 팔로우 여부
	 */
	private YesNo isFollowing(Photographer photographer, Long memberId) {
		return Boolean.TRUE.equals(
				followingRepository.existsByFollowingIdAndFollowerId(photographer.getMember().getId(), memberId)) ?
				YesNo.Y : YesNo.N;
	}

	private List<PhotographerDto.Detail> getPhotographerDetailByMemberGeoInfo(
			List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> results, Long customerId, Long areaId) {
		return results.stream()
				.map(result -> {
					Long memberId = Long.parseLong(result.getContent().getName().toString());
					Photographer photographer = photographerRepository.findByMemberIdAndActive(memberId, YesNo.Y)
							.orElseThrow(() ->  ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
					// 주활동지역에 해당 areaId가 포함되어 있는지 확인
					YesNo isOurPhotographer = photographer.getActiveAreas().stream()
							.anyMatch(activeArea -> activeArea.getArea().getId().equals(areaId))
							? YesNo.Y : YesNo.N;

					return PhotographerDto.Detail.of(
							photographer,
							(long) result.getDistance().getValue(),
							getFollowers(photographer),
							isFollowing(photographer, customerId),
							isOurPhotographer
					);
				})
				.toList();
	}
}
