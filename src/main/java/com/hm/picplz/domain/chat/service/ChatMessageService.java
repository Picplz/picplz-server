package com.hm.picplz.domain.chat.service;

import com.hm.picplz.domain.chat.domain.ChatMessage;
import com.hm.picplz.domain.chat.domain.ChatMessageDocument;
import com.hm.picplz.domain.chat.domain.ChatRoom;
import com.hm.picplz.domain.chat.dto.ChatMessageDto;
import com.hm.picplz.domain.chat.error.ChatErrorCode;
import com.hm.picplz.domain.chat.repository.ChatMessageRepository;
import com.hm.picplz.domain.chat.repository.ChatRoomRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.service.MemberService;
import com.hm.picplz.global.error.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 메시지 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberService memberService;

    /**
     * 메시지 전송
     *
     * @param request 메시지 전송 요청
     * @param senderId 전송자 Member ID
     * @return 전송된 메시지 정보
     */
    @Transactional
    public ChatMessageDto.Response sendMessage(ChatMessageDto.SendRequest request, Long senderId) {
        // 채팅방 조회 및 권한 확인
        ChatRoom chatRoom = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isMember(senderId)) {
            throw ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // Member 정보 조회
        Member member = memberService.getMemberById(senderId);

        // 현재 역할 확인 (작가인지 고객인지)
        String senderRole = determineRole(chatRoom, senderId);

        // 메시지 생성
        ChatMessage message = request.toDomain(
                senderId,
                member.getNickname(),
                senderRole,
                member.getProfileImage()
        );

        // MongoDB에 저장
        log.debug("MongoDB 저장 시작: message={}", message);
        ChatMessageDocument document = ChatMessageDocument.from(message);
        log.debug("Document 변환 완료: document={}", document);

        ChatMessageDocument savedDocument = chatMessageRepository.save(document);
        log.info("MongoDB 저장 성공: savedDocumentId={}, roomId={}, senderId={}",
                savedDocument.getId(), request.getRoomId(), senderId);

        // ChatRoom 메타데이터 업데이트 (MySQL)
        chatRoom.updateLastMessage(message.getContent(), message.getType(), senderId);

        // 상대방 읽지 않음 카운트 증가
        Long otherMemberId = chatRoom.getOtherMemberId(senderId);
        chatRoom.incrementUnreadCount(otherMemberId);

        log.info("메시지 전송 완료: roomId={}, senderId={}, messageId={}",
                request.getRoomId(), senderId, savedDocument.getId());

        return ChatMessageDto.Response.from(savedDocument.toDomain());
    }

    /**
     * 현재 Member의 채팅방에서의 역할 판단
     *
     * @param chatRoom 채팅방
     * @param memberId Member ID
     * @return "PHOTOGRAPHER" 또는 "CUSTOMER"
     */
    private String determineRole(ChatRoom chatRoom, Long memberId) {
        if (chatRoom.getPhotographer().getMember().getId().equals(memberId)) {
            return "PHOTOGRAPHER";
        } else if (chatRoom.getCustomer().getMember().getId().equals(memberId)) {
            return "CUSTOMER";
        }
        throw ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
    }

    /**
     * 채팅방의 메시지 목록 조회
     *
     * @param roomId 채팅방 ID
     * @param memberId 현재 로그인한 Member ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 메시지 목록
     */
    public ChatMessageDto.ListResponse getMessages(Long roomId, Long memberId, int page, int size) {
        // 채팅방 조회 및 권한 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isMember(memberId)) {
            throw ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // MongoDB에서 메시지 조회 (최신순)
        Pageable pageable = PageRequest.of(page, size);
        List<ChatMessageDocument> documents = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);

        List<ChatMessage> messages = documents.stream()
                .map(ChatMessageDocument::toDomain)
                .collect(Collectors.toList());

        // 전체 메시지 개수
        Long totalCount = chatMessageRepository.countByRoomId(roomId);

        // 다음 페이지 존재 여부
        boolean hasNext = (long) (page + 1) * size < totalCount;

        log.info("메시지 목록 조회: roomId={}, memberId={}, page={}, size={}, total={}",
                roomId, memberId, page, size, totalCount);

        return ChatMessageDto.ListResponse.of(roomId, messages, totalCount.intValue(), hasNext);
    }

    /**
     * 메시지 읽음 처리
     * 채팅방의 모든 메시지를 읽음 처리하고 unreadCount를 리셋
     *
     * @param roomId 채팅방 ID
     * @param memberId 현재 로그인한 Member ID
     */
    @Transactional
    public void markAsRead(Long roomId, Long memberId) {
        // 채팅방 조회 및 권한 확인
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        if (!chatRoom.isMember(memberId)) {
            throw ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // MongoDB: readBy에 memberId가 없는 메시지 조회 및 업데이트
        List<ChatMessageDocument> unreadMessages = chatMessageRepository
                .findByRoomIdOrderByCreatedAtDesc(roomId, Pageable.unpaged())
                .stream()
                .filter(doc -> !doc.getReadBy().contains(memberId))
                .collect(Collectors.toList());

        for (ChatMessageDocument document : unreadMessages) {
            document.addReadBy(memberId);
            chatMessageRepository.save(document);
        }

        // MySQL: ChatRoom의 unreadCount 리셋
        chatRoom.resetUnreadCount(memberId);

        log.info("메시지 읽음 처리 완료: roomId={}, memberId={}, unreadCount={}",
                roomId, memberId, unreadMessages.size());
    }
}
