package com.hm.picplz.domain.chat.controller;

import com.hm.picplz.domain.chat.dto.ChatMessageDto;
import com.hm.picplz.domain.chat.dto.ChatRoomDto;
import com.hm.picplz.domain.chat.service.ChatMessageService;
import com.hm.picplz.domain.chat.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Chat Room", description = "채팅방 관리 API")
@RestController
@RequestMapping("/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    /**
     * 채팅방 목록 조회
     * 역할에 따라 다른 채팅방 목록을 반환
     *
     * @param memberId 현재 로그인한 Member ID
     * @param currentRole 현재 역할 ("PHOTOGRAPHER", "CUSTOMER")
     * @return 채팅방 목록
     */
    @Operation(summary = "채팅방 목록 조회", description = "현재 역할에 따라 작가 또는 고객으로 참여한 채팅방 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ChatRoomDto.ListResponse> getMyChatRooms(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Parameter(description = "현재 역할 (PHOTOGRAPHER 또는 CUSTOMER)")
            @RequestHeader(value = "X-Current-Role", required = false) String currentRole
    ) {
        log.info("채팅방 목록 조회 요청: memberId={}, currentRole={}", memberId, currentRole);
        ChatRoomDto.ListResponse response = chatRoomService.getMyChatRooms(memberId, currentRole);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 생성 또는 조회
     * 동일한 작가와 고객 간의 채팅방이 존재하면 기존 채팅방을 반환
     *
     * @param request 채팅방 생성 요청 (photographerId, customerId)
     * @param memberId 현재 로그인한 Member ID
     * @return 생성되거나 조회된 채팅방 정보
     */
    @Operation(summary = "채팅방 생성 또는 조회", description = "새 채팅방을 생성하거나 기존 채팅방을 반환합니다.")
    @PostMapping
    public ResponseEntity<ChatRoomDto.Response> createChatRoom(
            @Valid @RequestBody ChatRoomDto.CreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        log.info("채팅방 생성 요청: memberId={}, photographerId={}, customerId={}",
                memberId, request.getPhotographerId(), request.getCustomerId());
        ChatRoomDto.Response response = chatRoomService.createOrGetChatRoom(request, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 상세 조회
     *
     * @param roomId 채팅방 ID
     * @param memberId 현재 로그인한 Member ID
     * @return 채팅방 상세 정보
     */
    @Operation(summary = "채팅방 상세 조회", description = "특정 채팅방의 상세 정보를 조회합니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDto.Response> getChatRoom(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        log.info("채팅방 상세 조회 요청: roomId={}, memberId={}", roomId, memberId);
        ChatRoomDto.Response response = chatRoomService.getChatRoom(roomId, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * 채팅방 메시지 목록 조회
     *
     * @param roomId 채팅방 ID
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param size 페이지 크기 (기본값: 50)
     * @param memberId 현재 로그인한 Member ID (JWT에서 추출)
     * @return 메시지 목록
     */
    @Operation(summary = "채팅방 메시지 목록 조회", description = "채팅방의 메시지 목록을 페이징하여 조회합니다.")
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ChatMessageDto.ListResponse> getMessages(
            @PathVariable Long roomId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "50") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        log.info("메시지 목록 조회 요청: roomId={}, memberId={}, page={}, size={}", roomId, memberId, page, size);
        ChatMessageDto.ListResponse response = chatMessageService.getMessages(roomId, memberId, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 메시지 읽음 처리
     * 채팅방의 모든 읽지 않은 메시지를 읽음 처리하고 unreadCount를 0으로 리셋
     *
     * @param roomId 채팅방 ID
     * @param memberId 현재 로그인한 Member ID (JWT에서 추출)
     * @return 204 No Content
     */
    @Operation(summary = "메시지 읽음 처리", description = "채팅방의 모든 읽지 않은 메시지를 읽음 처리합니다.")
    @PatchMapping("/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        log.info("메시지 읽음 처리 요청: roomId={}, memberId={}", roomId, memberId);
        chatMessageService.markAsRead(roomId, memberId);
        chatRoomService.resetUnreadCount(roomId, memberId);
        return ResponseEntity.noContent().build();
    }
}
