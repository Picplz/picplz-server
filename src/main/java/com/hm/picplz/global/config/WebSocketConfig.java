package com.hm.picplz.global.config;

import com.hm.picplz.domain.auth.jwt.JwtTokenProvider;
import com.hm.picplz.domain.chat.error.ChatErrorCode;
import com.hm.picplz.global.error.BaseErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 설정
 * - STOMP 프로토콜을 사용한 WebSocket 메시징 설정
 * - JWT 기반 인증 처리
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 메시지 브로커 설정
     * - /topic: 1:N 브로드캐스트 (채팅방 전체 메시지)
     * - /queue: 1:1 메시징 (개인 알림 등)
     * - /app: 클라이언트가 메시지를 보낼 때 사용하는 prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커가 처리할 prefix 설정 (구독 경로)
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 메시지를 보낼 때 사용할 prefix 설정
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트 등록
     * - WebSocket 연결 엔드포인트: /ws
     * - SockJS fallback 지원 (WebSocket을 지원하지 않는 브라우저 대응)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // CORS 설정
                .withSockJS();  // SockJS fallback 활성화
    }

    /**
     * 클라이언트 인바운드 채널 설정
     * - JWT 토큰 검증을 위한 Interceptor 추가
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // CONNECT 시 JWT 토큰 검증
                    String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                        log.error("WebSocket 연결 실패: Authorization 헤더가 없거나 잘못되었습니다.");
                        throw new BaseErrorException(ChatErrorCode.WEBSOCKET_AUTH_ERROR);
                    }

                    String token = authorizationHeader.substring(7);

                    if (!jwtTokenProvider.validateToken(token)) {
                        log.error("WebSocket 연결 실패: 유효하지 않은 JWT 토큰입니다.");
                        throw new BaseErrorException(ChatErrorCode.WEBSOCKET_AUTH_ERROR);
                    }

                    // 인증 정보 추출 및 저장
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    accessor.setUser(() -> String.valueOf(authentication.getPrincipal()));

                    log.info("WebSocket 연결 성공: memberId={}", authentication.getPrincipal());
                }

                return message;
            }
        });
    }
}
