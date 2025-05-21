package com.hm.picplz.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
	KAKAO("kakao"),
	APPLE("apple");

	private final String name;
}
