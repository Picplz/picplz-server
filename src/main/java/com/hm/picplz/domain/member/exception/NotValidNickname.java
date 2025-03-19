package com.hm.picplz.domain.member.exception;

import com.hm.picplz.global.error.BaseErrorException;

public class NotValidNickname extends BaseErrorException {
	public static final NotValidNickname EXCEPTION = new NotValidNickname();

	private NotValidNickname() {
		super(MemberErrorCode.NOT_VALID_NICKNAME);
	}
}
