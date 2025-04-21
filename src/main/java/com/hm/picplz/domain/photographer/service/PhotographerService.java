package com.hm.picplz.domain.photographer.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.area.domain.Area;
import com.hm.picplz.domain.following.repository.FollowingRepository;
import com.hm.picplz.domain.member.MemberRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.photographer.domain.ActiveArea;
import com.hm.picplz.domain.photographer.domain.Career;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.dto.PhotoMoodDto;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.ActiveAreaRepository;
import com.hm.picplz.domain.photographer.repository.AreaRepoisotry;
import com.hm.picplz.domain.photographer.repository.CareerRepository;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.global.common.entity.YesNo;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotographerService {

	private final PhotoMoodService photoMoodService;

	private final CareerRepository careerRepository;
	private final PhotographerRepository photographerRepository;
	private final MemberRepository memberRepository;
	private final FollowingRepository followingRepository;
	private final ActiveAreaRepository activeAreaRepository;
	private final AreaRepoisotry areaRepoisotry;

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String PHOTOGRAPHER_REDIS_KEY = "photographer:";

	@Transactional
	public void createPhotographer(Long memberId, PhotographerDto.Create createPhotographerRequestDto) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));

		Photographer photographer = Photographer.builder()
			.member(member)
			.period(createPhotographerRequestDto.getYear() * 12 + createPhotographerRequestDto.getMonth())
			.active(YesNo.N)
			.instagram(createPhotographerRequestDto.getInstagram())
			.introduction(createPhotographerRequestDto.getIntroduction())
			.build();

		photographerRepository.save(photographer);
		photoMoodService.createPhotoMoods(createPhotographerRequestDto.getPhotoMoods(), photographer);
		createActiveAreas(createPhotographerRequestDto.getActiveAreas(), photographer);
	}

	private void createActiveAreas(List<PhotographerDto.ActiveAreaReq> areaDtos, Photographer photographer) {
		for (PhotographerDto.ActiveAreaReq dto : areaDtos) {
			Area area = areaRepoisotry.findById(dto.getCode())
				.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.WRONG_AREA_CODE));

			ActiveArea activeArea = ActiveArea.builder()
				.photographer(photographer)
				.area(area)
				.sido(area.getSido())
				.sigungu(area.getSigungu())
				.eupmyeondong(area.getEupmyeondong())
				.ri(area.getRi())
				.name(area.getName())
				.priority(dto.getPriority())
				.build();

			activeAreaRepository.save(activeArea);
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
	public void addCareer(PhotographerDto.AddCareer addCareerRequestDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		List<Career> careers = addCareerRequestDto.getCareers()
			.stream()
			.map(career ->
				Career.builder()
					.type(career)
					.photographer(photographer)
				.build())
			.toList();
		careerRepository.saveAll(careers);
	}

	@Transactional
	public void updateCareerPeriod(PhotographerDto.UpdateCareerPeriod updateCareerPeriodRequestDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photographer.updatePeriod(
			updateCareerPeriodRequestDto.getYear() * 12 + updateCareerPeriodRequestDto.getMonth());
	}

	@Transactional
	public void addPhotoMood(PhotoMoodDto.PhotoMoodReq addPhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.addPhotoMood(addPhotoMoodDto.getPhotoMood(), photographer);
	}

	@Transactional
	public void deletePhotoMood(PhotoMoodDto.PhotoMoodReq deletePhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.deletePhotoMood(deletePhotoMoodDto.getPhotoMood(), photographer);
	}

}
