package com.hm.picplz.domain.review.dto;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.review.domain.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 작성 요청")
public class CreateReviewRequest {
    @NotNull(message = "작가 ID는 필수입니다.")
    @Schema(description = "작가 ID")
    private Long photographerId;

    @NotNull(message = "별점은 필수입니다.")
    @Min(value = 1, message = "별점은 1.0 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5.0 이하이어야 합니다.")
    @Schema(description = "별점 (1.0 ~ 5.0)", example = "4.5")
    private Float rating;

    @Size(max = 1000, message = "리뷰 내용은 1000자 이하이어야 합니다.")
    @Schema(description = "리뷰 내용", example = "정말 좋은 경험이었습니다!")
    private String content;

    @Schema(description = "리뷰 사진 URL", example = "[\"review/.jpg\", \"review/.jpg\"]")
    private List<String> photoUrls;

    public Review toEntity(Photographer photographer, Customer customer) {
        return Review.builder()
                .photographer(photographer)
                .customer(customer)
                .starPoint(rating)
                .content(content)
                .build();
    }
}
