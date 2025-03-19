package com.hm.picplz.domain.member.exception;

import static org.springframework.http.HttpStatus.*;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

    /* Member */
    MEMBER_NOT_FOUND(BAD_REQUEST, "MEMBER_404_1", "해당 회원이 존재하지 않습니다."),

    /* 중복 에러*/
    DUPLICATE_NICKNAME(BAD_REQUEST, "MEMBER_400_1", "이미 사용중인 닉네임입니다."),
    NOT_VALID_NICKNAME(BAD_REQUEST, "MEMBER_400_2", "사용 불가능한 닉네임입니다.")
    ;


    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
