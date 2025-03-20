package com.hm.picplz.global.error;

public class ExceptionFactory {
	public static BaseErrorException of(BaseErrorCode errorCode) {
		return new BaseErrorException(errorCode);
	}

	private ExceptionFactory() {}
}
