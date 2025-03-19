package com.hm.picplz.domain.member.exception;

import com.hm.picplz.global.error.BaseErrorException;

public class DuplicateNickname extends BaseErrorException {
	public static final DuplicateNickname EXCEPTION = new DuplicateNickname();

	private DuplicateNickname() {
		super(MemberErrorCode.DUPLICATE_NICKNAME);
	}
}