package com.hm.picplz.domain.photographer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.following.repository.FollowingRepository;
import com.hm.picplz.domain.member.MemberRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.global.common.entity.YesNo;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class PhotographerService {

    private final PhotographerRepository photographerRepository;
    private final MemberRepository memberRepository;
    private final PhotoMoodService photoMoodService;
    private final FollowingRepository followingRepository;

    @Transactional
    public void createPhotographer(Long memberId, PhotographerDto.Create createPhotographerRequestDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(()-> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));

        Photographer photographer = Photographer.builder()
                .member(member)
                .area(createPhotographerRequestDto.getArea())
                .period(createPhotographerRequestDto.getPeriod())
                .active(YesNo.N)
                .instagram(createPhotographerRequestDto.getInstagram())
                .introduction(createPhotographerRequestDto.getIntroduction())
                .build();

        photographerRepository.save(photographer);

        photoMoodService.createPhotoMood(createPhotographerRequestDto.getPhotoMoods(), photographer);
    }

    public PhotographerDto.Detail getPhotographerDetail(Long photographerId, Long memberId) {
        Photographer photographer = getPhotographer(photographerId);
        int followersCount = getFollowers(photographer);
        YesNo isFollowing = isFollowing(photographer, memberId);
        return PhotographerDto.Detail.of(photographer, followersCount, isFollowing);
    }

    private Photographer getPhotographer(Long photographerId) {
        return photographerRepository.findPhotographerWithMoods(photographerId)
            .orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
    }

    private int getFollowers(Photographer photographer) {
        return Math.toIntExact(followingRepository.countByFollowingId(photographer.getMember().getId()));
    }

    private YesNo isFollowing(Photographer photographer, Long memberId) {
        return Boolean.TRUE.equals(
            followingRepository.existsByFollowingIdAndFollowerId(photographer.getMember().getId(), memberId)) ?
            YesNo.Y : YesNo.N;
    }
}
