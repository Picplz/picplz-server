package com.hm.picplz.domain.review.exception;

import static org.springframework.http.HttpStatus.*;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {

    /* Review */
    REVIEW_NOT_FOUND(NOT_FOUND, "REVIEW_404_1", "해당 리뷰가 존재하지 않습니다."),
    PHOTOGRAPHER_NOT_FOUND(NOT_FOUND, "REVIEW_404_2", "해당 작가가 존재하지 않습니다."),
    CUSTOMER_NOT_FOUND(NOT_FOUND, "REVIEW_404_3", "해당 고객이 존재하지 않습니다."),

    /* Validation */
    INVALID_RATING(BAD_REQUEST, "REVIEW_400_1", "별점은 1.0에서 5.0 사이의 값이어야 합니다."),
    INVALID_REVIEW_CONTENT(BAD_REQUEST, "REVIEW_400_2", "별점 3~5점은 리뷰 내용이 최소 10자 이상이어야 합니다."),
    TOO_MANY_PHOTOS(BAD_REQUEST, "REVIEW_400_3", "리뷰 이미지는 최대 10개까지 업로드할 수 있습니다."),

    /* Authorization */
    UNAUTHORIZED_REVIEW_ACCESS(FORBIDDEN, "REVIEW_403_1", "해당 리뷰를 수정/삭제할 권한이 없습니다."),
    ONLY_CUSTOMER_CAN_WRITE_REVIEW(FORBIDDEN, "REVIEW_403_2", "리뷰는 고객만 작성할 수 있습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
