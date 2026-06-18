package com.hm.picplz.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    CUSTOMER("ROLE_CUSTOMER", "고객"),
    PHOTOGRAPHER("ROLE_PHOTOGRAPHER", "작가");

    private final String key;
    private final String title;
}
