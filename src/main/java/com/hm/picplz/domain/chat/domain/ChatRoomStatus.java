package com.hm.picplz.domain.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomStatus {
    ACTIVE("활성화"),
    BLOCKED("차단됨");

    private final String description;
}
