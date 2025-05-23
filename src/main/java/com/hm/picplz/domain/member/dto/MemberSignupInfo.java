package com.hm.picplz.domain.member.dto;

import com.hm.picplz.domain.member.domain.SocialProvider;

public interface MemberSignupInfo {
	String getNickname();
	String getSocialEmail();
	SocialProvider getSocialProvider();
	String getSocialCode();
	String getProfileImage();
}