package com.hm.picplz.domain.chat.domain;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.reservation.domain.Reservation;
import com.hm.picplz.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 데이터를 관리하는 MySQL 엔티티
 * 실제 메시지는 MongoDB에 저장
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private Photographer photographer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @NotNull
    @Column(nullable = false)
    private Integer totalMessageCount = 0;

    @NotNull
    @Column(nullable = false)
    private Integer unreadCountPhotographer = 0;

    @NotNull
    @Column(nullable = false)
    private Integer unreadCountCustomer = 0;

    @Size(max = 200)
    @Column(length = 200)
    private String lastMessageContent;

    @Enumerated(EnumType.STRING)
    private MessageType lastMessageType;

    @Column(name = "last_message_sender_id")
    private Long lastMessageSenderId;

    private LocalDateTime lastMessageAt;

    @Builder
    private ChatRoom(Photographer photographer, Customer customer, Reservation reservation) {
        this.photographer = photographer;
        this.customer = customer;
        this.reservation = reservation;
        this.status = ChatRoomStatus.ACTIVE;
        this.totalMessageCount = 0;
        this.unreadCountPhotographer = 0;
        this.unreadCountCustomer = 0;
    }

    /**
     * 새 메시지 전송 시 채팅방 데이터 업데이트
     */
    public void updateLastMessage(String content, MessageType type, Long senderId) {
        this.lastMessageContent = content != null && content.length() > 200
            ? content.substring(0, 200)
            : content;
        this.lastMessageType = type;
        this.lastMessageSenderId = senderId;
        this.lastMessageAt = LocalDateTime.now();
        this.totalMessageCount++;
    }

    /**
     * 읽지 않은 메시지 카운트 증가
     */
    public void incrementUnreadCount(Long memberId) {
        if (photographer.getMember().getId().equals(memberId)) {
            this.unreadCountPhotographer++;
        } else if (customer.getMember().getId().equals(memberId)) {
            this.unreadCountCustomer++;
        }
    }

    /**
     * 읽지 않은 메시지 카운트 리셋
     */
    public void resetUnreadCount(Long memberId) {
        if (photographer.getMember().getId().equals(memberId)) {
            this.unreadCountPhotographer = 0;
        } else if (customer.getMember().getId().equals(memberId)) {
            this.unreadCountCustomer = 0;
        }
    }

    /**
     * 채팅방 차단
     */
    public void block() {
        this.status = ChatRoomStatus.BLOCKED;
    }

    /**
     * 채팅방 활성화
     */
    public void activate() {
        this.status = ChatRoomStatus.ACTIVE;
    }

    /**
     * 상대방 Member ID 조회
     */
    public Long getOtherMemberId(Long memberId) {
        if (photographer.getMember().getId().equals(memberId)) {
            return customer.getMember().getId();
        } else if (customer.getMember().getId().equals(memberId)) {
            return photographer.getMember().getId();
        }
        throw new IllegalArgumentException("해당 Member는 이 채팅방의 참여자가 아닙니다.");
    }

    /**
     * 해당 Member가 채팅방 참여자인지 확인
     */
    public boolean isMember(Long memberId) {
        return photographer.getMember().getId().equals(memberId) || customer.getMember().getId().equals(memberId);
    }
}
