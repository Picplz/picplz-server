package com.hm.picplz.infra.s3;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3ErrorCode implements BaseErrorCode {

	WRONG_IMAGE_FILE_TYPE(BAD_REQUEST, "IMAGE_400_1", "png, jpeg 파일을 업로드 해주세요.")

	;

	private final HttpStatus status;
	private final String code;
	private final String reason;

	@Override
	public ErrorReason getErrorReason() {
		return ErrorReason.of(status.value(), code, reason);
	}
}
