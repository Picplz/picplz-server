package com.hm.picplz.domain.chat.error;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

/**
 * 채팅 도메인 에러 코드
 */
@Getter
@AllArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {

    /* 채팅방 관련 에러 */
    CHAT_ROOM_NOT_FOUND(NOT_FOUND, "CHAT_404_1", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(FORBIDDEN, "CHAT_403_1", "해당 채팅방에 접근 권한이 없습니다."),
    CHAT_ROOM_BLOCKED(FORBIDDEN, "CHAT_403_2", "차단된 채팅방입니다."),
    INVALID_CHAT_ROOM_PARTICIPANTS(BAD_REQUEST, "CHAT_400_1", "채팅방 참여자 정보가 올바르지 않습니다."),

    /* 메시지 관련 에러 */
    MESSAGE_NOT_FOUND(NOT_FOUND, "CHAT_404_2", "메시지를 찾을 수 없습니다."),
    EMPTY_MESSAGE_CONTENT(BAD_REQUEST, "CHAT_400_2", "메시지 내용이 비어있습니다."),
    MESSAGE_TOO_LONG(BAD_REQUEST, "CHAT_400_3", "메시지가 너무 깁니다. (최대 1000자)"),
    INVALID_MESSAGE_TYPE(BAD_REQUEST, "CHAT_400_4", "올바르지 않은 메시지 타입입니다."),

    /* Photographer/Customer 관련 에러 */
    PHOTOGRAPHER_NOT_FOUND(NOT_FOUND, "CHAT_404_3", "작가를 찾을 수 없습니다."),
    CUSTOMER_NOT_FOUND(NOT_FOUND, "CHAT_404_4", "고객을 찾을 수 없습니다."),

    /* WebSocket 연결 에러 */
    WEBSOCKET_CONNECTION_ERROR(INTERNAL_SERVER_ERROR, "CHAT_500_1", "WebSocket 연결 중 오류가 발생했습니다."),
    WEBSOCKET_AUTH_ERROR(UNAUTHORIZED, "CHAT_401_1", "WebSocket 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
