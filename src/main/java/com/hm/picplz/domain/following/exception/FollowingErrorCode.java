package com.hm.picplz.domain.following.exception;

import static org.springframework.http.HttpStatus.*;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FollowingErrorCode implements BaseErrorCode {

    ALREADY_FOLLOWING(CONFLICT, "FOLLOWING_409_1", "이미 팔로우한 작가입니다."),
    NOT_FOLLOWING(BAD_REQUEST, "FOLLOWING_400_1", "팔로우하지 않은 작가입니다."),
    CANNOT_FOLLOW_SELF(BAD_REQUEST, "FOLLOWING_400_2", "자기 자신을 팔로우할 수 없습니다."),
    MEMBER_NOT_FOUND(NOT_FOUND, "FOLLOWING_404_1", "해당 회원이 존재하지 않습니다."),
    PHOTOGRAPHER_NOT_FOUND(NOT_FOUND, "FOLLOWING_404_2", "해당 작가가 존재하지 않습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
