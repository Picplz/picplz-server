package com.hm.picplz.domain.chat.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * MongoDB에 저장되는 채팅 메시지 Document
*/
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_messages")
@CompoundIndexes({
    @CompoundIndex(name = "room_created_idx", def = "{'room_id': 1, 'created_at': -1}"),
    @CompoundIndex(name = "room_status_idx", def = "{'room_id': 1, 'status': 1}")
})
public class ChatMessageDocument {

    @Id
    private String id;

    @Indexed
    @Field("room_id")
    private Long roomId;

    @Field("sender")
    private SenderInfo sender;

    @Field("type")
    private MessageType type;

    @Field("content")
    private String content;

    @Field("read_by")
    private Set<Long> readBy;

    @Field("status")
    private MessageStatus status;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Builder
    private ChatMessageDocument(String id, Long roomId, SenderInfo sender, MessageType type,
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

    /**
     * 도메인 모델로부터 Document 생성
     */
    public static ChatMessageDocument from(ChatMessage message) {
        return ChatMessageDocument.builder()
            .id(message.getId())
            .roomId(message.getRoomId())
            .sender(SenderInfo.from(message.getSender()))
            .type(message.getType())
            .content(message.getContent())
            .readBy(new HashSet<>(message.getReadBy()))
            .status(message.getStatus())
            .createdAt(message.getCreatedAt())
            .build();
    }

    /**
     * Document를 도메인 모델로 변환
     */
    public ChatMessage toDomain() {
        return ChatMessage.builder()
            .id(this.id)
            .roomId(this.roomId)
            .sender(this.sender.toDomain())
            .type(this.type)
            .content(this.content)
            .readBy(new HashSet<>(this.readBy))
            .status(this.status)
            .createdAt(this.createdAt)
            .build();
    }

    /**
     * 읽음 처리
     */
    public void addReadBy(Long memberId) {
        this.readBy.add(memberId);
        if (this.status == MessageStatus.SENT || this.status == MessageStatus.DELIVERED) {
            this.status = MessageStatus.READ;
        }
    }

    /**
     * Embedded Document: 발신자 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class SenderInfo {
        private Long id;
        private String name;
        private String role;
        private String profileImage;

        @Builder
        private SenderInfo(Long id, String name, String role, String profileImage) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.profileImage = profileImage;
        }

        public static SenderInfo from(ChatMessage.MessageSender sender) {
            return SenderInfo.builder()
                .id(sender.getId())
                .name(sender.getName())
                .role(sender.getRole())
                .profileImage(sender.getProfileImage())
                .build();
        }

        public ChatMessage.MessageSender toDomain() {
            return ChatMessage.MessageSender.builder()
                .id(this.id)
                .name(this.name)
                .role(this.role)
                .profileImage(this.profileImage)
                .build();
        }
    }
}
