package com.hm.picplz.domain.auth.error;

import static org.springframework.http.HttpStatus.*;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    INVALID_REFRESH_TOKEN(UNAUTHORIZED, "AUTH_401_1", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "AUTH_401_2", "만료된 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(UNAUTHORIZED, "AUTH_401_3", "리프레시 토큰이 존재하지 않습니다. 다시 로그인해주세요."),
    REFRESH_TOKEN_MISMATCH(UNAUTHORIZED, "AUTH_401_4", "리프레시 토큰이 일치하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
