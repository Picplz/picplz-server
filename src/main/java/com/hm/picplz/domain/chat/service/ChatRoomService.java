package com.hm.picplz.domain.chat.service;

import com.hm.picplz.domain.chat.domain.ChatRoom;
import com.hm.picplz.domain.chat.dto.ChatRoomDto;
import com.hm.picplz.domain.chat.error.ChatErrorCode;
import com.hm.picplz.domain.chat.repository.ChatRoomRepository;
import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.customer.repository.CustomerRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.member.repository.MemberRepository;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.global.error.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 채팅방 관련 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final PhotographerRepository photographerRepository;
    private final CustomerRepository customerRepository;
    private final MemberRepository memberRepository;
    private final CacheManager cacheManager;

    /**
     * 채팅방 생성 또는 조회
     * 동일한 작가와 고객 간의 채팅방이 이미 존재하면 기존 채팅방을 반환
     *
     * @param request 채팅방 생성 요청 (photographerId, customerId 포함)
     * @param memberId 현재 로그인한 Member ID
     * @return 생성되거나 조회된 채팅방 정보
     */
    @Transactional
    public ChatRoomDto.Response createOrGetChatRoom(ChatRoomDto.CreateRequest request, Long memberId) {
        // Photographer와 Customer 조회
        Photographer photographer = photographerRepository.findById(request.getPhotographerId())
                .orElseThrow(() -> ExceptionFactory.of(ChatErrorCode.PHOTOGRAPHER_NOT_FOUND));
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> ExceptionFactory.of(ChatErrorCode.CUSTOMER_NOT_FOUND));

        // 현재 Member가 참여자인지 확인 (작가 또는 고객)
        if (!photographer.getMember().getId().equals(memberId) &&
            !customer.getMember().getId().equals(memberId)) {
            throw ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        // 기존 채팅방 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByPhotographerAndCustomer(photographer, customer);

        if (existingRoom.isPresent()) {
            log.info("기존 채팅방 반환: roomId={}", existingRoom.get().getId());
            return ChatRoomDto.Response.from(existingRoom.get(), memberId);
        }

        // 새 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .photographer(photographer)
                .customer(customer)
                .reservation(null)  // 예약 연결은 추후 구현
                .build();

        chatRoomRepository.save(chatRoom);
        log.info("새 채팅방 생성: roomId={}, photographerId={}, customerId={}",
                chatRoom.getId(), photographer.getId(), customer.getId());

        // 채팅방 참여자 양쪽의 캐시만 무효화
        evictChatRoomsCacheForMembers(photographer.getMember().getId(), customer.getMember().getId());

        return ChatRoomDto.Response.from(chatRoom, memberId);
    }

    /**
     * 현재 Member의 채팅방 목록 조회
     *
     * @param memberId 현재 로그인한 Member ID
     * @return 역할에 맞는 채팅방 목록
     */
    @Cacheable(value = "chatRooms", key = "#memberId")
    public ChatRoomDto.ListResponse getMyChatRooms(Long memberId) {
        // 1. Member 조회 및 현재 role 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));

        Role currentRole = member.getRole();
        List<ChatRoom> chatRooms;

        // 2. 현재 역할에 따라 다른 쿼리 실행
        if (currentRole == Role.PHOTOGRAPHER) {
            chatRooms = chatRoomRepository.findByPhotographerMemberId(memberId);
            log.info("작가 채팅방 목록 조회: memberId={}, count={}", memberId, chatRooms.size());
        } else if (currentRole == Role.CUSTOMER) {
            chatRooms = chatRoomRepository.findByCustomerMemberId(memberId);
            log.info("고객 채팅방 목록 조회: memberId={}, count={}", memberId, chatRooms.size());
        } else {
            // GUEST 역할이거나 role이 없는 경우 빈 목록 반환
            log.warn("채팅방 조회 불가: memberId={}, role={} (PHOTOGRAPHER 또는 CUSTOMER 역할 필요)", memberId, currentRole);
            chatRooms = List.of();
        }

        // 3. 응답 생성
        List<ChatRoomDto.Response> responses = chatRooms.stream()
                .map(room -> ChatRoomDto.Response.from(room, memberId))
                .collect(Collectors.toList());

        return ChatRoomDto.ListResponse.builder()
                .chatRooms(responses)
                .totalCount(responses.size())
                .currentRole(currentRole.name())
                .build();
    }

    /**
     * 특정 채팅방 상세 조회
     *
     * @param chatRoomId 조회할 채팅방 ID
     * @param memberId 현재 로그인한 Member ID
     * @return 채팅방 상세 정보
     */
    @Cacheable(value = "chatRoom", key = "#chatRoomId + '_' + #memberId")
    public ChatRoomDto.Response getChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 접근 권한 확인
        if (!chatRoom.isMember(memberId)) {
            throw ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        return ChatRoomDto.Response.from(chatRoom, memberId);
    }

    /**
     * 읽지 않은 메시지 카운트 리셋
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId 현재 로그인한 Member ID
     */
    @Transactional
    public void resetUnreadCount(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방 접근 권한 확인
        if (!chatRoom.isMember(memberId)) {
            throw ExceptionFactory.of(ChatErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }

        chatRoom.resetUnreadCount(memberId);

        // 해당 사용자의 채팅방 목록 캐시와 상세 캐시 무효화
        evictChatRoomCaches(chatRoomId, memberId);

        log.info("읽지 않은 메시지 카운트 리셋: roomId={}, memberId={}", chatRoomId, memberId);
    }

    /**
     * 특정 멤버들의 채팅방 목록 캐시 무효화
     *
     * @param photographerMemberId 작가 Member ID
     * @param customerMemberId 고객 Member ID
     */
    private void evictChatRoomsCacheForMembers(Long photographerMemberId, Long customerMemberId) {
        try {
            cacheManager.getCache("chatRooms").evict(photographerMemberId);
            cacheManager.getCache("chatRooms").evict(customerMemberId);
            log.debug("채팅방 목록 캐시 무효화: photographerMemberId={}, customerMemberId={}",
                    photographerMemberId, customerMemberId);
        } catch (Exception e) {
            log.warn("채팅방 목록 캐시 무효화 실패: photographerMemberId={}, customerMemberId={}",
                    photographerMemberId, customerMemberId, e);
        }
    }

    /**
     * 특정 채팅방과 멤버의 캐시 무효화
     *
     * @param chatRoomId 채팅방 ID
     * @param memberId Member ID
     */
    private void evictChatRoomCaches(Long chatRoomId, Long memberId) {
        try {
            // 채팅방 단건 캐시 무효화
            String chatRoomKey = chatRoomId + "_" + memberId;
            cacheManager.getCache("chatRoom").evict(chatRoomKey);

            // 채팅방 목록 캐시 무효화
            cacheManager.getCache("chatRooms").evict(memberId);

            log.debug("채팅방 캐시 무효화: chatRoomId={}, memberId={}", chatRoomId, memberId);
        } catch (Exception e) {
            log.warn("채팅방 캐시 무효화 실패: chatRoomId={}, memberId={}", chatRoomId, memberId, e);
        }
    }
}
