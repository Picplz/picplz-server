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
    PHOTOGRAPHER_NOT_FOUND(BAD_REQUEST, "PHOTOGRAPHER_404_1", "해당 작가가 존재하지 않습니다."),

    /* Career */
    WRONG_CAREER_TYPE(BAD_REQUEST, "CAREER_400_1", "유효하지 않은 경력 유형입니다."),

    /* Area */
    WRONG_AREA_CODE(BAD_REQUEST, "AREA_400_1", "유효하지 않은 법정동 정보입니다."),

    /* PhotoMood */
    PHOTOMOOD_NOT_FOUND(BAD_REQUEST, "PHOTOMOOD_404_1", "해당 사진 감성이 존재하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
