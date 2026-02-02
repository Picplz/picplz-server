package com.hm.picplz.domain.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageStatus {
    SENT("전송됨"),
    DELIVERED("전달됨"),
    READ("읽음"),
    FAILED("실패");

    private final String description;
}
