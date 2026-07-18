package com.hm.picplz.domain.review.service;

import com.hm.picplz.domain.review.dto.*;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    //작가 리뷰 목록 조회
    ReviewListResponse getReviewsByPhotographer(Long photographerId, Pageable pageable);

    // 작가 평균 별점 및 리뷰 개수
    PhotographerRatingResponse getPhotographerRating(Long photographerId);

    // 리뷰 상세
    ReviewDetailResponse getReviewDetail(Long reviewId);

    // 리뷰 작성
    ReviewDetailResponse createReview(Long memberId, CreateReviewRequest request);

    // 리뷰 수정
    ReviewDetailResponse updateReview(Long memberId, Long reviewId, UpdateReviewRequest request);

    // 리뷰 삭제
    void deleteReview(Long memberId, Long reviewId);
}
