package com.hm.picplz.domain.following.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.following.domain.Following;
import com.hm.picplz.domain.following.dto.FollowingDto;
import com.hm.picplz.domain.following.exception.FollowingErrorCode;
import com.hm.picplz.domain.following.repository.FollowingRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.repository.MemberRepository;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.domain.review.repository.ReviewRepository;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowingService {

    private final FollowingRepository followingRepository;
    private final MemberRepository memberRepository;
    private final PhotographerRepository photographerRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public void follow(Long followerMemberId, Long photographerId) {
        Member follower = memberRepository.findById(followerMemberId)
                .orElseThrow(() -> ExceptionFactory.of(FollowingErrorCode.MEMBER_NOT_FOUND));

        Photographer photographer = photographerRepository.findById(photographerId)
                .orElseThrow(() -> ExceptionFactory.of(FollowingErrorCode.PHOTOGRAPHER_NOT_FOUND));

        Member following = photographer.getMember();

        if (following.getId().equals(followerMemberId)) {
            throw ExceptionFactory.of(FollowingErrorCode.CANNOT_FOLLOW_SELF);
        }

        if (followingRepository.existsByFollowingIdAndFollowerId(following.getId(), followerMemberId)) {
            throw ExceptionFactory.of(FollowingErrorCode.ALREADY_FOLLOWING);
        }

        followingRepository.save(Following.of(following, follower));
    }

    @Transactional
    public void unfollow(Long followerMemberId, Long photographerId) {
        Photographer photographer = photographerRepository.findById(photographerId)
                .orElseThrow(() -> ExceptionFactory.of(FollowingErrorCode.PHOTOGRAPHER_NOT_FOUND));

        Long followingMemberId = photographer.getMember().getId();

        if (!followingRepository.existsByFollowingIdAndFollowerId(followingMemberId, followerMemberId)) {
            throw ExceptionFactory.of(FollowingErrorCode.NOT_FOLLOWING);
        }

        followingRepository.deleteByFollowingIdAndFollowerId(followingMemberId, followerMemberId);
    }

    public FollowingDto.IsFollowingResponse checkFollowing(Long followerMemberId, Long photographerId) {
        Photographer photographer = photographerRepository.findById(photographerId)
                .orElseThrow(() -> ExceptionFactory.of(FollowingErrorCode.PHOTOGRAPHER_NOT_FOUND));

        boolean isFollowing = followingRepository.existsByFollowingIdAndFollowerId(
                photographer.getMember().getId(), followerMemberId);

        return FollowingDto.IsFollowingResponse.of(isFollowing);
    }

    // 팔로잉한 작가 목록 조회
    @Transactional(readOnly = true)
    public List<FollowingDto.PhotographerCard> getFollowingPhotographers(Long followerMemberId, String sort) {
        List<Following> followings = followingRepository.findAllWithPhotographerByFollowerId(followerMemberId);

        List<Photographer> photographers = followings.stream()
                .map(f -> f.getFollowing().getPhotographer())
                .filter(Objects::nonNull)
                .toList();

        return photographers.stream()
                .map(FollowingDto.PhotographerCard::from)
                .toList();
    }
}
