package com.hm.picplz.domain.photographer.service;

import java.time.Duration;
import java.util.List;

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
import com.hm.picplz.domain.photographer.repository.PhotographerCameraRepository;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.global.common.entity.YesNo;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotographerService {

	private final MemberService memberService;
	private final PhotoMoodService photoMoodService;
	private final PhotographerRepository photographerRepository;
	private final FollowingRepository followingRepository;
	private final ActiveAreaRepository activeAreaRepository;
	private final AreaRepository areaRepository;
	private final PhotographerCameraRepository photographerCameraRepository;

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String PHOTOGRAPHER_REDIS_KEY = "photographer:";

	/**
	 * 작가 회원가입 메서드
	 * @param createPhotographerRequest 작가 회원가입을 위한 입력 정보
	 */
	@Transactional
	public void createPhotographer(PhotographerDto.CreatePhotographerRequest createPhotographerRequest) {
		// 마지막 닉네임 중복 확인
		memberService.checkNickname(createPhotographerRequest.getNickname());
		// 멤버 데이터 생성
		Member member = memberService.createMember(MemberDto.CreateMemberRequest.of(createPhotographerRequest,
			Role.PHOTOGRAPHER));

		// 작가 정보 생성
		Photographer photographer = Photographer.from(member);
		photographerRepository.save(photographer);

		// 작가 분위기 키워드
		photoMoodService.createPhotoMoods(createPhotographerRequest.getPhotoMoods(), photographer);
		// 작가 주 촬영지
		createActiveAreas(createPhotographerRequest.getActiveAreas(), photographer);
		// 작가 촬영 카메라
		createPhotographerCameras(createPhotographerRequest.getCameras(), photographer);
	}

	/**
	 * 작가의 주 촬영지(법정동 데이터 기반) 목록을 받아 N:M 관계 테이블에 저장
	 * @param areaDtos 주 촬영지 목록
	 * @param photographer 작가
	 */
	private void createActiveAreas(List<PhotographerDto.ActiveAreaRequest> areaDtos, Photographer photographer) {
		for (PhotographerDto.ActiveAreaRequest dto : areaDtos) {
			Area area = areaRepository.findById(dto.getCode())
				.orElseThrow(() -> ExceptionFactory.of(AreaErrorCode.WRONG_AREA_CODE));

			ActiveArea activeArea = ActiveArea.builder()
				.photographer(photographer)
				.area(area)
				.priority(dto.getPriority())
				.build();

			activeAreaRepository.save(activeArea);
		}
	}

	/**
	 * 작가의 촬영 기기 1:N 테이블 저장
	 * @param cameraDtos 작가의 촬영 기기
	 * @param photographer 작가
	 */
	private void createPhotographerCameras(List<PhotographerDto.PhotographerCameraRequest> cameraDtos, Photographer photographer) {
		for (PhotographerDto.PhotographerCameraRequest dto : cameraDtos) {
			PhotographerCamera camera = PhotographerCamera.builder()
				.photographer(photographer)
				.type(dto.getType())
				.brand(dto.getBrand())
				.name(dto.getName())
				.cameraBrand(dto.getCameraBrand())
				.build();

			photographerCameraRepository.save(camera);
		}
	}

	/**
	 * 작가 상세 정보 반환
	 * @param photographerId 조회하려는 작가 정보
	 * @param memberId 조회를 시도하는 회원 정보(팔로우 여부를 위해)
	 * @return
	 */
	public PhotographerDto.Detail getPhotographerDetail(Long photographerId, Long memberId) {
		Photographer photographer = getPhotographerByPhotographerId(photographerId);
		int followersCount = getFollowers(photographer);
		YesNo isFollowing = isFollowing(photographer, memberId);
		return PhotographerDto.Detail.of(photographer, followersCount, isFollowing);
	}

	/**
	 * 작가 entity 데이터 조회 메서드
	 * @param photographerId 작가 pk
	 * @return
	 */
	private Photographer getPhotographerByPhotographerId(Long photographerId) {
		return photographerRepository.findPhotographerWithMoods(photographerId)
			.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	/**
	 * 작가 enitty 데이터 조회 메서드
	 * @param memberId 작가의 member pk
	 * @return
	 */
	private Photographer getPhotographerByMemberId(Long memberId) {
		return photographerRepository.findByMemberId(memberId)
			.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	/**
	 * 작가 권한 확인을 위해 캐시 확인
	 * @param memberId
	 * @return 작가의 권한 확인 결과가 redis에 존재하는지
	 */
	public Boolean getCachedPhotographerExistence(Long memberId) {
		return (Boolean)redisTemplate.opsForValue().get(PHOTOGRAPHER_REDIS_KEY + memberId);
	}

	/**
	 * 작가인 경우, redis에 캐시로 저장해 작가 권한을 빠르게 확인하도록 한다.
	 * @param memberId
	 * @return 작가인지 아닌지 반환 (작가라면 redis에 캐싱)
	 */
	public boolean cachePhotographerExistence(Long memberId) {
		boolean exists = photographerRepository.existsByMemberId(memberId);
		redisTemplate.opsForValue().set(PHOTOGRAPHER_REDIS_KEY + memberId, exists, Duration.ofMinutes(30));
		return exists;
	}

	/**
	 * member Id로 작가인지 확인하고, 캐시가 없다면 작가 여부를 MySQL에서 확인하고 나서 반환한다.
	 * @param memberId
	 * @return
	 */
	public boolean checkAndCachePhotographer(Long memberId) {
		Boolean cached = getCachedPhotographerExistence(memberId);
		if (cached != null) {
			return cached;
		}
		return cachePhotographerExistence(memberId);
	}

	/**
	 * 작가의 팔로워 수를 조회
	 * @param photographer
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

}
