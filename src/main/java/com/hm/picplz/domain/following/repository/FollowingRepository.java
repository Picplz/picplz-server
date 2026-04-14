package com.hm.picplz.domain.following.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hm.picplz.domain.following.domain.Following;

public interface FollowingRepository extends JpaRepository<Following, Long> {
	Long countByFollowingId(Long userId);
	Boolean existsByFollowingIdAndFollowerId(Long followingId, Long followerId);

	void deleteByFollowingIdAndFollowerId(Long followingId, Long followerId);

	@Query("SELECT f FROM Following f " +
		"JOIN FETCH f.following fm " +
		"JOIN FETCH fm.photographer p " +
		"LEFT JOIN FETCH p.photoMoods " +
		"WHERE f.follower.id = :followerId " +
		"ORDER BY f.createdAt DESC")
	List<Following> findAllWithPhotographerByFollowerId(@Param("followerId") Long followerId);
}
