package com.hm.picplz.domain.chat.service;

import com.hm.picplz.domain.chat.domain.ChatMessage;
import com.hm.picplz.domain.chat.domain.ChatMessageDocument;
import com.hm.picplz.domain.chat.domain.ChatRoom;
import com.hm.picplz.domain.chat.domain.MessageStatus;
import com.hm.picplz.domain.chat.dto.ChatMessageDto;
import com.hm.picplz.domain.chat.error.ChatErrorCode;
import com.hm.picplz.domain.chat.repository.ChatMessageRepository;
import com.hm.picplz.domain.chat.repository.ChatRoomRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.service.MemberService;
import com.hm.picplz.global.error.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    private final MongoTemplate mongoTemplate;
    private final CacheManager cacheManager;

    /**
     * 메시지 전송
     *
     * @param request 메시지 전송 요청
     * @param senderId 전송자 Member ID
     * @return 전송된 메시지 정보
     */
    @Transactional
    public ChatMessageDto.Response sendMessage(ChatMessageDto.SendRequest request, Long senderId) {
        // 채팅방 조회
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
        ChatMessageDocument document = ChatMessageDocument.from(message);

        ChatMessageDocument savedDocument = chatMessageRepository.save(document);

        // ChatRoom 메타데이터 업데이트 (MySQL)
        chatRoom.updateLastMessage(message.getContent(), message.getType(), senderId);

        // 상대방 읽지 않음 카운트 증가
        Long otherMemberId = chatRoom.getOtherMemberId(senderId);
        chatRoom.incrementUnreadCount(otherMemberId);

        // 해당 채팅방의 메시지 캐시만 무효화 (첫 페이지 캐시)
        evictChatMessagesCache(request.getRoomId());

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
     * 캐싱 전략:
     * - 첫 페이지(page=0, size=50) 캐싱 (채팅방 입장 시 최근 메시지)
     * - TTL: 2분 (CacheConfig에서 설정)
     *
     * @param roomId 채팅방 ID
     * @param memberId 현재 로그인한 Member ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 메시지 목록
     */
    @Cacheable(value = "chatMessages", key = "#roomId + '_' + #page + '_' + #size",
               condition = "#page == 0 and #size == 50")  // 첫 페이지만 캐싱
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
     * MongoDB 벌크 업데이트를 사용하여 N번의 쿼리를 1번으로 최적화
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

        // MongoDB: readBy에 memberId가 없는 메시지를 벌크 업데이트
        Query query = new Query();
        query.addCriteria(Criteria.where("room_id").is(roomId)
                .and("read_by").ne(memberId));

        Update update = new Update();
        update.addToSet("read_by", memberId);
        update.set("status", MessageStatus.READ);

        // 벌크 업데이트 실행 (updateMulti: 조건에 맞는 모든 문서 업데이트)
        long updatedCount = mongoTemplate.updateMulti(query, update, ChatMessageDocument.class).getModifiedCount();

        // MySQL: ChatRoom의 unreadCount 리셋
        chatRoom.resetUnreadCount(memberId);

        log.info("메시지 읽음 처리 완료 (벌크 업데이트): roomId={}, memberId={}, updatedCount={}",
                roomId, memberId, updatedCount);
    }

    /**
     * 특정 채팅방의 메시지 캐시 무효화
     *
     * @param roomId 채팅방 ID
     */
    private void evictChatMessagesCache(Long roomId) {
        try {
            // 첫 페이지 캐시만 무효화 (page=0, size=50)
            String cacheKey = roomId + "_0_50";
            cacheManager.getCache("chatMessages").evict(cacheKey);
            log.debug("채팅 메시지 캐시 무효화: roomId={}, cacheKey={}", roomId, cacheKey);
        } catch (Exception e) {
            log.warn("채팅 메시지 캐시 무효화 실패: roomId={}", roomId, e);
        }
    }
}
