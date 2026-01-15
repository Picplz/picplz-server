package com.hm.picplz.domain.review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hm.picplz.domain.review.domain.Review;
import com.hm.picplz.domain.review.domain.ReviewPhoto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 요약 정보")
public class ReviewSummary {
    @Schema(description = "리뷰 ID", example = "1")
    private Long reviewId;

    @Schema(description = "고객 ID", example = "1")
    private Long customerId;

    @Schema(description = "고객 닉네임", example = "홍길동")
    private String customerNickname;

    @Schema(description = "고객 프로필 이미지 URL")
    private String customerProfileImage;

    @Schema(description = "별점", example = "4.5")
    private Float rating;

    @Schema(description = "리뷰 내용", example = "정말 좋은 경험이었습니다!")
    private String content;

    @Schema(description = "리뷰 사진 리스트")
    private List<ReviewPhotoInfo> photos;

    @Schema(description = "작성일시", example = "2024-01-01T10:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static ReviewSummary from(Review review, List<ReviewPhoto> photos) {
        return ReviewSummary.builder()
                .reviewId(review.getId())
                .customerId(review.getCustomer().getId())
                .customerNickname(review.getCustomer().getMember().getNickname())
                .customerProfileImage(review.getCustomer().getMember().getProfileImage())
                .rating(review.getStarPoint())
                .content(review.getContent())
                .photos(photos.stream()
                        .map(ReviewPhotoInfo::from)
                        .collect(Collectors.toList()))
                .createdAt(review.getCreatedAt())
                .build();
    }
}
