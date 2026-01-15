package com.hm.picplz.domain.review.repository;

import com.hm.picplz.domain.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 작가 리뷰 목록 페이징
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.photographer p " +
            "LEFT JOIN FETCH r.customer c " +
            "LEFT JOIN FETCH c.member m " +
            "WHERE r.photographer.id = :photographerId")
    Page<Review> findByPhotographerId(@Param("photographerId") Long photographerId, Pageable pageable);

    // 작가 평균 별점
    @Query("SELECT AVG(r.starPoint) FROM Review r WHERE r.photographer.id = :photographerId")
    Optional<Float> findAverageRatingByPhotographerId(@Param("photographerId") Long photographerId);

    // 작가의 리뷰 개수
    @Query("SELECT COUNT(r) FROM Review r WHERE r.photographer.id = :photographerId")
    Long countByPhotographerId(@Param("photographerId") Long photographerId);

    // 리뷰 상세 조회
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.photographer p " +
            "LEFT JOIN FETCH r.customer c " +
            "LEFT JOIN FETCH c.member m " +
            "WHERE r.id = :reviewId")
    Optional<Review> findByIdWithDetails(@Param("reviewId") Long reviewId);
}
