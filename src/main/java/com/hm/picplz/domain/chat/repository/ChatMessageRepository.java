package com.hm.picplz.domain.chat.repository;

import com.hm.picplz.domain.chat.domain.ChatMessageDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB 채팅 메시지 Repository
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageDocument, String> {

    /**
     * 채팅방 ID로 메시지 조회 (최신순, 페이징)
     */
    List<ChatMessageDocument> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    /**
     * 읽지 않은 메시지 개수 조회
     * readBy에 memberId가 포함되지 않은 메시지 개수
     */
    Long countByRoomIdAndReadByNotContaining(Long roomId, Long memberId);

    /**
     * 채팅방의 모든 메시지 개수 조회
     */
    Long countByRoomId(Long roomId);
}
