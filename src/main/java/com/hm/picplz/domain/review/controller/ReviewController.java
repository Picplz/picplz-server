package com.hm.picplz.domain.review.controller;

import com.hm.picplz.domain.review.dto.*;
import com.hm.picplz.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reviews")
@Tag(name = "Review", description = "리뷰 관리 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "고객이 작가에 대한 리뷰를 작성합니다." + "\n" +
            "별점 1~2 : 리뷰 선택사항" + " / 별점 3~5 : 최소 10자 이상 필수입력" + "\n" +
            " / 사진 업로드 10초과 제한")
    @PostMapping
    public ReviewDetailResponse createReview(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(memberId, request);
    }

    @Operation(summary = "리뷰 상세 조회", description = "리뷰의 상세 정보를 조회합니다.")
    @GetMapping("/{reviewId}")
    public ReviewDetailResponse getReviewDetail(
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId) {
        return reviewService.getReviewDetail(reviewId);
    }

    @Operation(summary = "리뷰 수정", description = "작성한 리뷰를 수정합니다. (작성자만 가능)")
    @PutMapping("/{reviewId}")
    public ReviewDetailResponse updateReview(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(memberId, reviewId, request);
    }

    @Operation(summary = "리뷰 삭제", description = "작성한 리뷰를 삭제합니다. (작성자만 가능)")
    @DeleteMapping("/{reviewId}")
    public void deleteReview(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId) {
        reviewService.deleteReview(memberId, reviewId);
    }
}
