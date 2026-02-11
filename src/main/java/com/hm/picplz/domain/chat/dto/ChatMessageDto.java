package com.hm.picplz.domain.chat.dto;

import com.hm.picplz.domain.chat.domain.ChatMessage;
import com.hm.picplz.domain.chat.domain.MessageStatus;
import com.hm.picplz.domain.chat.domain.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatMessageDto {

    /**
     * 메시지 전송 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SendRequest {

        @NotNull(message = "채팅방 ID는 필수입니다.")
        private Long roomId;

        @NotNull(message = "메시지 타입은 필수입니다.")
        private MessageType type;

        @NotBlank(message = "메시지 내용은 필수입니다.")
        @Size(max = 1000, message = "메시지는 최대 1000자까지 입력 가능합니다.")
        private String content;

        /**
         * 도메인 모델로 변환
         */
        public ChatMessage toDomain(Long senderId, String senderName, String senderRole, String profileImage) {
            return ChatMessage.builder()
                    .roomId(roomId)
                    .sender(ChatMessage.MessageSender.builder()
                            .id(senderId)
                            .name(senderName)
                            .role(senderRole)
                            .profileImage(profileImage)
                            .build())
                    .type(type)
                    .content(content)
                    .status(MessageStatus.SENT)
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 메시지 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id;
        private Long roomId;
        private SenderResponse sender;
        private MessageType type;
        private String content;
        private Set<Long> readBy;
        private MessageStatus status;
        private LocalDateTime createdAt;

        /**
         * 도메인 모델로부터 생성
         */
        public static Response from(ChatMessage message) {
            return Response.builder()
                    .id(message.getId())
                    .roomId(message.getRoomId())
                    .sender(SenderResponse.from(message.getSender()))
                    .type(message.getType())
                    .content(message.getContent())
                    .readBy(message.getReadBy())
                    .status(message.getStatus())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }

    /**
     * 메시지 리스트 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private Long roomId;
        private List<Response> messages;
        private Integer totalCount;
        private Boolean hasNext;

        /**
         * 메시지 리스트로부터 생성
         */
        public static ListResponse of(Long roomId, List<ChatMessage> messages, Integer totalCount, Boolean hasNext) {
            return ListResponse.builder()
                    .roomId(roomId)
                    .messages(messages.stream()
                            .map(Response::from)
                            .collect(Collectors.toList()))
                    .totalCount(totalCount)
                    .hasNext(hasNext)
                    .build();
        }
    }

    /**
     * 발신자 정보 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderResponse {
        private Long id;
        private String name;
        private String role;
        private String profileImage;

        /**
         * 도메인 모델로부터 생성
         */
        public static SenderResponse from(ChatMessage.MessageSender sender) {
            return SenderResponse.builder()
                    .id(sender.getId())
                    .name(sender.getName())
                    .role(sender.getRole())
                    .profileImage(sender.getProfileImage())
                    .build();
        }
    }
}
