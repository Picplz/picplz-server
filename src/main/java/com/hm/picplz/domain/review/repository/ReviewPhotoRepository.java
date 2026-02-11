package com.hm.picplz.domain.review.repository;

import com.hm.picplz.domain.review.domain.ReviewPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewPhotoRepository extends JpaRepository<ReviewPhoto, Long> {


    @Query("SELECT rp FROM ReviewPhoto rp WHERE rp.review.id = :reviewId ORDER BY rp.photoOrder ASC")
    List<ReviewPhoto> findByReviewIdOrderByPhotoOrder(@Param("reviewId") Long reviewId);

    @Query("SELECT rp FROM ReviewPhoto rp WHERE rp.review.id IN :reviewIds ORDER BY rp.photoOrder ASC")
    List<ReviewPhoto> findByReviewIdIn(@Param("reviewIds") List<Long> reviewIds);

    @Modifying
    @Query("DELETE FROM ReviewPhoto rp WHERE rp.review.id = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);
}
