package com.hm.picplz.domain.auth.error;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum AppleErrorCode implements BaseErrorCode {

    INVALID_TOKEN(BAD_REQUEST, "APPLE_400_1", "Apple 의 토큰이 올바르지 않습니다"),
    EXPIRED_TOKEN(UNAUTHORIZED, "APPLE_401_1", "만료된 Apple 토큰입니다"),
    FAILED_CREATE_PUBLIC_KEY(UNAUTHORIZED, "APPLE_401_2", "Public key 를 만들 수 없습니다"),
    PUBLIC_KEY_NOT_FOUND(NOT_FOUND, "APPLE_404_1", "Apple 공개키를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
