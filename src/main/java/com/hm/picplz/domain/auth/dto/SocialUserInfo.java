package com.hm.picplz.domain.auth.dto;

import com.hm.picplz.domain.member.domain.SocialProvider;

public interface SocialUserInfo {
    String getSocialEmail();
    SocialProvider getSocialProvider();
    String getSocialCode();
}
