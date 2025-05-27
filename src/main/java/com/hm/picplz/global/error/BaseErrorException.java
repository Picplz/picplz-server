package com.hm.picplz.global.error;

import lombok.Getter;

@Getter
public class BaseErrorException extends RuntimeException {
    private final BaseErrorCode errorCode;

    public BaseErrorException(BaseErrorCode errorCode) {
        super(errorCode.getErrorReason().getReason()); // ✅ 메시지를 부모에 전달
        this.errorCode = errorCode;
    }

    public ErrorReason getErrorReason() {
        return this.errorCode.getErrorReason();
    }
}
