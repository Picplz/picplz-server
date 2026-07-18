package com.hm.picplz.domain.chat.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**한 건의 메시지를 관리하는 도메인*/
@Getter
public class ChatMessage {

    private String id;  // MongoDB ObjectId
    private Long roomId;  // ChatRoom의 ID
    private MessageSender sender;
    private MessageType type;
    private String content;
    private Set<Long> readBy;  // 읽은 사용자 ID 목록
    private MessageStatus status;
    private LocalDateTime createdAt;

    @Builder
    private ChatMessage(String id, Long roomId, MessageSender sender, MessageType type,
                       String content, Set<Long> readBy, MessageStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.readBy = readBy != null ? readBy : new HashSet<>();
        this.status = status != null ? status : MessageStatus.SENT;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**메시지 읽음 처리*/
    public void markAsRead(Long memberId) {
        this.readBy.add(memberId);
        if (this.status == MessageStatus.SENT || this.status == MessageStatus.DELIVERED) {
            this.status = MessageStatus.READ;
        }
    }

    /**메시지가 특정 사용자에게 읽혔는지 확인*/
    public boolean isReadBy(Long memberId) {
        return this.readBy.contains(memberId);
    }

    /**
     * 메시지 전송 실패 처리
     */
    public void markAsFailed() {
        this.status = MessageStatus.FAILED;
    }

    /**
     * 메시지 전달 완료 처리
     */
    public void markAsDelivered() {
        if (this.status == MessageStatus.SENT) {
            this.status = MessageStatus.DELIVERED;
        }
    }

    /**
     * Role 어떻게 할지 확인 필요
     */
    @Getter
    @Builder
    public static class MessageSender {
        private Long id;
        private String name;
        private String role;
        private String profileImage;

        public static MessageSender of(Long id, String name, String role, String profileImage) {
            return MessageSender.builder()
                .id(id)
                .name(name)
                .role(role)
                .profileImage(profileImage)
                .build();
        }
    }
}
