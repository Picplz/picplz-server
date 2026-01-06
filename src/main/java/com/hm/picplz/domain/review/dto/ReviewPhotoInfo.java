package com.hm.picplz.domain.review.dto;

import com.hm.picplz.domain.review.domain.ReviewPhoto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "리뷰 사진 정보")
public class ReviewPhotoInfo {
    @Schema(description = "사진 ID", example = "1")
    private Long photoId;

    @Schema(description = "사진 URL", example = "review/.jpg")
    private String imageUrl;

    @Schema(description = "사진 순서", example = "1")
    private Integer photoOrder;

    public static ReviewPhotoInfo from(ReviewPhoto photo) {
        return ReviewPhotoInfo.builder()
                .photoId(photo.getId())
                .imageUrl(photo.getImage())
                .photoOrder(photo.getPhotoOrder())
                .build();
    }
}
