package com.hm.picplz.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "작가 평점 정보")
public class PhotographerRatingResponse {
    @Schema(description = "평균 별점")
    private Float averageRating;

    @Schema(description = "총 리뷰 개수")
    private Long totalReviews;
}
