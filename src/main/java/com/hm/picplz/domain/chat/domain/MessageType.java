package com.hm.picplz.domain.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    TEXT("텍스트 메시지"),
    IMAGE("이미지 메시지"),
    SYSTEM("시스템 메시지");

    private final String description;
}
