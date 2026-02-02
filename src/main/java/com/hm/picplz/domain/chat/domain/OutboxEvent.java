package com.hm.picplz.domain.chat.domain;

import com.hm.picplz.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Outbox Pattern을 위한 이벤트 저장 엔티티
 * MongoDB와 MySQL 간의 일관성을 보장하기 위해 사용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "chat_outbox_events",
    indexes = {
        @Index(name = "idx_unprocessed", columnList = "processed,createdAt")
    }
)
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_event_id", updatable = false)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 50)
    private String aggregateType;  // CHAT_MESSAGE, CHAT_ROOM

    @NotNull
    @Column(nullable = false, length = 24)
    private String aggregateId;  // MongoDB ObjectId or Entity ID

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventType eventType;

    @NotNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;  // JSON 형태의 이벤트 데이터

    @NotNull
    @Column(nullable = false)
    private Boolean processed = false;

    @NotNull
    @Column(nullable = false)
    private Integer retryCount = 0;

    private LocalDateTime processedAt;

    @Builder
    private OutboxEvent(String aggregateType, String aggregateId, OutboxEventType eventType,
                       String payload, Boolean processed, Integer retryCount) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.processed = processed != null ? processed : false;
        this.retryCount = retryCount != null ? retryCount : 0;
    }

    /**
     * 메시지 전송 이벤트 생성
     */
    public static OutboxEvent messageSent(String messageId, Long roomId, Long senderId, String content) {
        String payload = String.format(
            "{\"messageId\":\"%s\",\"roomId\":%d,\"senderId\":%d,\"content\":\"%s\"}",
            messageId, roomId, senderId, escapeJson(content)
        );
        return OutboxEvent.builder()
            .aggregateType("CHAT_MESSAGE")
            .aggregateId(messageId)
            .eventType(OutboxEventType.MESSAGE_SENT)
            .payload(payload)
            .build();
    }

    /**
     * 메시지 읽음 이벤트 생성
     */
    public static OutboxEvent messageRead(String messageId, Long roomId, Long readerId) {
        String payload = String.format(
            "{\"messageId\":\"%s\",\"roomId\":%d,\"readerId\":%d}",
            messageId, roomId, readerId
        );
        return OutboxEvent.builder()
            .aggregateType("CHAT_MESSAGE")
            .aggregateId(messageId)
            .eventType(OutboxEventType.MESSAGE_READ)
            .payload(payload)
            .build();
    }

    /**
     * 채팅방 생성 이벤트 생성
     */
    public static OutboxEvent roomCreated(Long roomId, Long photographerId, Long customerId) {
        String payload = String.format(
            "{\"roomId\":%d,\"photographerId\":%d,\"customerId\":%d}",
            roomId, photographerId, customerId
        );
        return OutboxEvent.builder()
            .aggregateType("CHAT_ROOM")
            .aggregateId(roomId.toString())
            .eventType(OutboxEventType.ROOM_CREATED)
            .payload(payload)
            .build();
    }

    /**
     * 이벤트 처리 완료 표시
     */
    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 재시도 카운트 증가
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * 최대 재시도 횟수 초과 확인
     */
    public boolean isMaxRetryExceeded(int maxRetryCount) {
        return this.retryCount >= maxRetryCount;
    }

    /**
     * JSON 문자열 이스케이프
     */
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}
