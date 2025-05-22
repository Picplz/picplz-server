package com.hm.picplz.domain.following.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.following.domain.Following;

public interface FollowingRepository extends JpaRepository<Following, Long> {
	Long countByFollowingId(Long userId);
	Boolean existsByFollowingIdAndFollowerId(Long followingId, Long followerId);
}
