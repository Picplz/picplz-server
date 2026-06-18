package com.hm.picplz.domain.photographer.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PhotographerErrorCode implements BaseErrorCode {

    /* Photographer */
    PHOTOGRAPHER_NOT_FOUND(NOT_FOUND, "PHOTOGRAPHER_404_1", "해당 작가가 존재하지 않습니다."),
    NOT_PHOTOGRAPHER(FORBIDDEN, "PHOTOGRAPHER_403_1", "사진 작가만 사용 가능한 기능입니다."),
    ALREADY_PHOTOGRAPHER(CONFLICT, "PHOTOGRAPHER_409_1", "이미 작가로 등록되어 있습니다."),

    /* PhotoMood */
    PHOTOMOOD_NOT_FOUND(NOT_FOUND, "PHOTOMOOD_404_1", "해당 사진 감성이 존재하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
