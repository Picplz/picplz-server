package com.hm.picplz.domain.following.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hm.picplz.domain.following.domain.Following;

@Repository
public interface FollowingRepository extends JpaRepository<Following, Long> {
	Long countByFollowingId(Long userId);
	Boolean existsByFollowingIdAndFollowerId(Long followingId, Long followerId);
}
