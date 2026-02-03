package com.hm.picplz.domain.review.controller;

import com.hm.picplz.domain.review.dto.PhotographerRatingResponse;
import com.hm.picplz.domain.review.dto.ReviewListResponse;
import com.hm.picplz.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/photographers")
@Tag(name = "Photographer Review", description = "작가별 리뷰 조회 API")
public class PhotographerReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "작가별 리뷰 목록 조회", description = "작가에 대한 리뷰 목록을 조회합니다.")
    @GetMapping("/{photographerId}/reviews")
    public ReviewListResponse getReviewsByPhotographer(
            @Parameter(description = "작가 ID", required = true)
            @PathVariable Long photographerId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 방식 (RECOMMENDED: 추천순, LATEST: 최신순)", example = "RECOMMENDED")
            @RequestParam(defaultValue = "RECOMMENDED") String sort) {


        Pageable pageable = createPageable(page, size, sort);
        return reviewService.getReviewsByPhotographer(photographerId, pageable);
    }

    @Operation(summary = "작가 평균 별점 및 리뷰 개수 조회", description = "작가의 평균 별점과 총 리뷰 개수를 조회합니다.")
    @GetMapping("/{photographerId}/rating")
    public PhotographerRatingResponse getPhotographerRating(
            @Parameter(description = "작가 ID", required = true)
            @PathVariable Long photographerId) {

        return reviewService.getPhotographerRating(photographerId);
    }

    private Pageable createPageable(int page, int size, String sortType) {
        Sort sort;
        if ("LATEST".equalsIgnoreCase(sortType)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else {
            // RECOMMENDED - 별점 총합 순 정렬
            sort = Sort.by(Sort.Direction.DESC, "starPoint");
        }
        return PageRequest.of(page, size, sort);
    }
}
