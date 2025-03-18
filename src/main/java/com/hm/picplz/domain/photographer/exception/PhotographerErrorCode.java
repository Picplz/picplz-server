package com.hm.picplz.domain.photographer.exception;

import static org.springframework.http.HttpStatus.*;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PhotographerErrorCode implements BaseErrorCode {

    /* Photographer */
    PHOTOGRAPHER_NOT_FOUND(BAD_REQUEST, "PHOTOGRAPHER_404_1", "해당 작가가 존재하지 않습니다.");

    private HttpStatus status;
    private String code;
    private String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
