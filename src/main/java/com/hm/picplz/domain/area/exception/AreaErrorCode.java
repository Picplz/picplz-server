package com.hm.picplz.domain.area.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AreaErrorCode implements BaseErrorCode {

	WRONG_AREA_CODE(BAD_REQUEST, "AREA_400_1", "유효하지 않은 법정동 정보입니다."),
	BAD_POSITION(BAD_REQUEST, "AREA_400_2", "사용자의 위치와 범위 값이 적절하지 않습니다.");


	private final HttpStatus status;
	private final String code;
	private final String reason;

	@Override
	public ErrorReason getErrorReason() {
		return ErrorReason.of(status.value(), code, reason);
	}
}
