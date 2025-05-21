package com.hm.picplz.domain.photographer.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.area.domain.Area;
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
import com.hm.picplz.domain.photographer.repository.AreaRepoisotry;
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
	private final AreaRepoisotry areaRepoisotry;
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

	private void createActiveAreas(List<PhotographerDto.ActiveAreaRequest> areaDtos, Photographer photographer) {
		for (PhotographerDto.ActiveAreaRequest dto : areaDtos) {
			Area area = areaRepoisotry.findById(dto.getCode())
				.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.WRONG_AREA_CODE));

			ActiveArea activeArea = ActiveArea.builder()
				.photographer(photographer)
				.area(area)
				.priority(dto.getPriority())
				.build();

			activeAreaRepository.save(activeArea);
		}
	}

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

	public PhotographerDto.Detail getPhotographerDetail(Long photographerId, Long memberId) {
		Photographer photographer = getPhotographerByPhotographerId(photographerId);
		int followersCount = getFollowers(photographer);
		YesNo isFollowing = isFollowing(photographer, memberId);
		return PhotographerDto.Detail.of(photographer, followersCount, isFollowing);
	}

	private Photographer getPhotographerByPhotographerId(Long photographerId) {
		return photographerRepository.findPhotographerWithMoods(photographerId)
			.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	private Photographer getPhotographerByMemberId(Long memberId) {
		return photographerRepository.findByMemberId(memberId)
			.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	public Boolean getCachedPhotographerExistence(Long memberId) {
		return (Boolean)redisTemplate.opsForValue().get(PHOTOGRAPHER_REDIS_KEY + memberId);
	}

	public boolean cachePhotographerExistence(Long memberId) {
		boolean exists = photographerRepository.existsByMemberId(memberId);
		redisTemplate.opsForValue().set(PHOTOGRAPHER_REDIS_KEY + memberId, exists, Duration.ofMinutes(30));
		return exists;
	}

	public boolean checkAndCachePhotographer(Long memberId) {
		Boolean cached = getCachedPhotographerExistence(memberId);
		if (cached != null) {
			return cached;
		}
		return cachePhotographerExistence(memberId);
	}

	private int getFollowers(Photographer photographer) {
		return Math.toIntExact(followingRepository.countByFollowingId(photographer.getMember().getId()));
	}

	private YesNo isFollowing(Photographer photographer, Long memberId) {
		return Boolean.TRUE.equals(
			followingRepository.existsByFollowingIdAndFollowerId(photographer.getMember().getId(), memberId)) ?
			YesNo.Y : YesNo.N;
	}

	@Transactional
	public void addPhotoMood(PhotoMoodDto.PhotoMoodRequest addPhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.addPhotoMood(addPhotoMoodDto.getPhotoMood(), photographer);
	}

	@Transactional
	public void deletePhotoMood(PhotoMoodDto.PhotoMoodRequest deletePhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.deletePhotoMood(deletePhotoMoodDto.getPhotoMood(), photographer);
	}

}
