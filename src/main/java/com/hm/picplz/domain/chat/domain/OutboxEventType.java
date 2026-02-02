package com.hm.picplz.domain.chat.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboxEventType {
    MESSAGE_SENT("메시지 전송됨"),
    MESSAGE_READ("메시지 읽음"),
    ROOM_CREATED("채팅방 생성됨");

    private final String description;
}
