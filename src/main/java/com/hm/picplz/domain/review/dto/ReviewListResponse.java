package com.hm.picplz.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 목록 조회 응답")
public class ReviewListResponse {
    @Schema(description = "평균 별점", example = "4.5")
    private Float averageRating;

    @Schema(description = "총 리뷰 개수", example = "100")
    private Long totalReviews;

    @Schema(description = "리뷰 목록")
    private List<ReviewSummary> reviews;
}
