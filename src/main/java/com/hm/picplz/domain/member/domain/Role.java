package com.hm.picplz.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    CUSTOMER("ROLE_GUEST", "고객"),
    PHOTOGRAPHER("ROLE_USER", "작가");

    private final String key;
    private final String title;
}
