package com.hm.picplz.domain.chat.dto;

import com.hm.picplz.domain.chat.domain.ChatRoom;
import com.hm.picplz.domain.chat.domain.ChatRoomStatus;
import com.hm.picplz.domain.chat.domain.MessageType;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.customer.domain.Customer;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomDto {

    /**
     * 채팅방 생성 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "작가 ID는 필수입니다.")
        private Long photographerId;

        @NotNull(message = "고객 ID는 필수입니다.")
        private Long customerId;

        private Long reservationId;  // Optional
    }

    /**
     * 채팅방 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private ParticipantInfo photographer;
        private ParticipantInfo customer;
        private Long reservationId;
        private ChatRoomStatus status;
        private Integer totalMessageCount;
        private Integer unreadCount;  // 현재 사용자 기준
        private String lastMessageContent;
        private MessageType lastMessageType;
        private Long lastMessageSenderId;
        private LocalDateTime lastMessageAt;
        private LocalDateTime createdAt;

        /**
         * ChatRoom 엔티티로부터 Response 생성
         *
         * @param chatRoom ChatRoom 엔티티
         * @param currentMemberId 현재 로그인한 사용자의 Member ID
         * @return ChatRoomDto.Response
         */
        public static Response from(ChatRoom chatRoom, Long currentMemberId) {
            // currentMemberId 기준으로 unreadCount 계산
            Integer unreadCount = 0;
            if (chatRoom.getPhotographer().getMember().getId().equals(currentMemberId)) {
                unreadCount = chatRoom.getUnreadCountPhotographer();
            } else if (chatRoom.getCustomer().getMember().getId().equals(currentMemberId)) {
                unreadCount = chatRoom.getUnreadCountCustomer();
            }

            return Response.builder()
                    .id(chatRoom.getId())
                    .photographer(ParticipantInfo.fromPhotographer(chatRoom.getPhotographer()))
                    .customer(ParticipantInfo.fromCustomer(chatRoom.getCustomer()))
                    .reservationId(chatRoom.getReservation() != null ? chatRoom.getReservation().getId() : null)
                    .status(chatRoom.getStatus())
                    .totalMessageCount(chatRoom.getTotalMessageCount())
                    .unreadCount(unreadCount)
                    .lastMessageContent(chatRoom.getLastMessageContent())
                    .lastMessageType(chatRoom.getLastMessageType())
                    .lastMessageSenderId(chatRoom.getLastMessageSenderId())
                    .lastMessageAt(chatRoom.getLastMessageAt())
                    .createdAt(chatRoom.getCreatedAt())
                    .build();
        }
    }

    /**
     * 채팅방 참여자 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantInfo {
        private Long memberId;
        private String nickname;
        private String profileImage;
        private String role;  // "PHOTOGRAPHER" or "CUSTOMER"

        /**
         * Photographer 엔티티로부터 ParticipantInfo 생성
         */
        public static ParticipantInfo fromPhotographer(Photographer photographer) {
            return ParticipantInfo.builder()
                    .memberId(photographer.getMember().getId())
                    .nickname(photographer.getMember().getNickname())
                    .profileImage(photographer.getMember().getProfileImage())
                    .role("PHOTOGRAPHER")
                    .build();
        }

        /**
         * Customer 엔티티로부터 ParticipantInfo 생성
         */
        public static ParticipantInfo fromCustomer(Customer customer) {
            return ParticipantInfo.builder()
                    .memberId(customer.getMember().getId())
                    .nickname(customer.getMember().getNickname())
                    .profileImage(customer.getMember().getProfileImage())
                    .role("CUSTOMER")
                    .build();
        }
    }

    /**
     * 채팅방 리스트 응답
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListResponse {
        private List<Response> chatRooms;
        private Integer totalCount;
        private String currentRole;  // "PHOTOGRAPHER" or "CUSTOMER"
    }
}
