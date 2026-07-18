package com.hm.picplz.domain.chat.controller;

import com.hm.picplz.domain.chat.dto.ChatMessageDto;
import com.hm.picplz.domain.chat.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;

/**
 * WebSocket STOMP Controller
 * 실시간 채팅 메시지 전송 처리
 *
 * 클라이언트 연결 예시:
 * 1. WebSocket 연결: ws://localhost:8080/api/v1/ws
 * 2. CONNECT 시 Authorization 헤더에 "Bearer {JWT_TOKEN}" 포함
 * 3. 채팅방 구독: /topic/room/{roomId}
 * 4. 메시지 전송: /app/chat/message
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

        private final ChatMessageService chatMessageService;
        private final SimpMessagingTemplate messagingTemplate;

        /**
         * 메시지 전송 처리
         * 클라이언트가 /app/chat/message로 메시지를 전송하면 이 메서드가 호출됨
         *
         * 처리 흐름:
         * 1. 메시지를 MongoDB에 저장
         * 2. ChatRoom 메타데이터 업데이트 (MySQL)
         * 3. 채팅방 구독자들에게 메시지 브로드캐스트 (/topic/room/{roomId})
         *
         * @param request  메시지 전송 요청
         * @param memberId 현재 로그인한 Member ID (WebSocket 인증에서 추출)
         */
        @MessageMapping("/chat/message")
        public void sendMessage(
                        @Valid @Payload ChatMessageDto.SendRequest request,
                        Principal principal) {
                Long memberId = Long.parseLong(principal.getName());
                try {
                        log.info("WebSocket 메시지 수신: roomId={}, senderId={}, type={}",
                                        request.getRoomId(), memberId, request.getType());

                        // 메시지 저장 및 처리
                        ChatMessageDto.Response response = chatMessageService.sendMessage(request, memberId);

                        // 채팅방 구독자들에게 메시지 전송
                        String destination = "/topic/room/" + request.getRoomId();
                        messagingTemplate.convertAndSend(destination, response);

                        log.info("메시지 브로드캐스트 완료: destination={}, messageId={}", destination, response.getId());
                } catch (Exception e) {
                        log.error("메시지 전송 중 오류 발생: roomId={}, senderId={}", request.getRoomId(), memberId, e);

                        // 에러 메시지를 발신자에게만 전송
                        messagingTemplate.convertAndSendToUser(
                                        String.valueOf(memberId),
                                        "/queue/errors",
                                        "메시지 전송에 실패했습니다: " + e.getMessage());
                }
        }

        /**
         * 입력 중 상태 브로드캐스트
         * 클라이언트가 /app/chat/typing으로 메시지를 전송하면 이 메서드가 호출됨
         *
         * @param typingInfo 입력 중 상태 정보
         */
        @MessageMapping("/chat/typing")
        public void sendTypingStatus(
                        @Payload TypingInfo typingInfo,
                        Principal principal) {
                Long memberId = Long.parseLong(principal.getName());
                log.debug("입력 중 상태 수신: roomId={}, memberId={}, isTyping={}",
                                typingInfo.roomId(), memberId, typingInfo.isTyping());

                // 입력 중 상태를 채팅방의 다른 참여자에게 전송
                messagingTemplate.convertAndSend(
                                "/topic/room/" + typingInfo.roomId() + "/typing",
                                new TypingStatus(memberId, typingInfo.isTyping()));
        }

        /**
         * 입력 중 상태 정보 DTO
         */
        public record TypingInfo(Long roomId, boolean isTyping) {
        }

        /**
         * 입력 중 상태 브로드캐스트 DTO
         */
        public record TypingStatus(Long memberId, boolean isTyping) {
        }
}
