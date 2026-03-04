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
    MEMBER_NOT_FOUND(NOT_FOUND, "MEMBER_404_1", "해당 회원이 존재하지 않습니다."),
    MEMBER_LOCATION_NOT_FOUND(NOT_FOUND, "MEMBER_404_2", "해당 회원의 위치가 존재하지 않습니다."),

    /* 중복 에러*/
    DUPLICATE_NICKNAME(BAD_REQUEST, "MEMBER_400_1", "이미 사용중인 닉네임입니다."),
    NOT_VALID_NICKNAME(BAD_REQUEST, "MEMBER_400_2", "사용 불가능한 닉네임입니다."),

    /* 프로필 관련 */
    PHOTOGRAPHER_PROFILE_NOT_FOUND(NOT_FOUND, "MEMBER_404_3", "작가 프로필이 없습니다. 작가 등록을 먼저 진행해주세요."),
    CUSTOMER_PROFILE_NOT_FOUND(NOT_FOUND, "MEMBER_404_4", "고객 프로필이 없습니다."),
    ALREADY_CUSTOMER(CONFLICT, "MEMBER_409_1", "이미 고객으로 등록되어 있습니다."),

    /* 소셜 관련 */
    NO_KAKAO_USER(BAD_REQUEST, "SOCIAL_400_1", "카카오 사용자 정보를 가져오지 못했습니다.")
    ;


    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
