package com.hm.picplz.domain.chat.repository;

import com.hm.picplz.domain.chat.domain.ChatRoom;
import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.photographer.domain.Photographer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채팅방 JPA Repository
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 작가와 고객으로 채팅방 조회
     */
    Optional<ChatRoom> findByPhotographerAndCustomer(Photographer photographer, Customer customer);

    /**
     * 특정 Member가 참여한 모든 채팅방 조회 (작가 또는 고객으로 참여)
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.photographer.member.id = :memberId " +
           "OR cr.customer.member.id = :memberId " +
           "ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findByMemberId(@Param("memberId") Long memberId);

    /**
     * 작가가 참여한 채팅방 조회
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.photographer.member.id = :memberId " +
           "ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findByPhotographerMemberId(@Param("memberId") Long memberId);

    /**
     * 고객이 참여한 채팅방 조회
     */
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.customer.member.id = :memberId " +
           "ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findByCustomerMemberId(@Param("memberId") Long memberId);
}
