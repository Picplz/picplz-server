package com.hm.picplz.domain.reservation.repository;

import com.hm.picplz.domain.reservation.domain.Reservation;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
        select r
        from Reservation r
        join fetch r.shootProduct sp
        join sp.photographer p
        join p.member m
        where m.id = :memberId
        order by r.createdAt desc
    """)
    List<Reservation> findAllByPhotographerMemberId(@Param("memberId") Long memberId);
}
