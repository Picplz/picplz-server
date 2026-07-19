package com.hm.picplz.domain.photographer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hm.picplz.domain.photographer.domain.Photographer;

public interface PhotographerRepository extends JpaRepository<Photographer, Long> {
    @Query("SELECT p FROM Photographer p JOIN FETCH p.member WHERE p.member.id = :memberId")
    Optional<Photographer> findByMemberId(@Param("memberId") Long memberId);

    @Query("""
    SELECT p
    FROM Photographer p
    LEFT JOIN p.reviews r
    WHERE p.member.nickname LIKE CONCAT('%', :keyword, '%')
    GROUP BY p
    ORDER BY
        COALESCE(AVG(r.starPoint), 0) DESC,
        COUNT(r) DESC,
        p.id DESC
        """)
    Page<Photographer> searchPhotographersByRating(
            @Param("keyword") String keyword,
            Pageable pageable
    );


    @Query("""
    SELECT p
    FROM Photographer p
    LEFT JOIN p.reviews r
    WHERE p.member.nickname LIKE CONCAT('%', :keyword, '%')
    GROUP BY p
    ORDER BY
        COUNT(r) DESC,
        COALESCE(AVG(r.starPoint), 0) DESC,
        p.id DESC
    """)
    Page<Photographer> searchPhotographersByReviewCount(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
    SELECT p
    FROM Photographer p
    LEFT JOIN p.reviews r
    LEFT JOIN Following f
        ON f.following = p.member
    WHERE p.member.nickname LIKE CONCAT('%', :keyword, '%')
    GROUP BY p
    ORDER BY
        COUNT(DISTINCT f) DESC,
        COALESCE(AVG(r.starPoint), 0) DESC,
        COUNT(DISTINCT r) DESC,
        p.id DESC
    """)
    Page<Photographer> searchPhotographersByFollowerCount(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT p FROM Photographer p JOIN FETCH p.member LEFT JOIN FETCH p.photoMoods WHERE p.id = :photographerId")
    Optional<Photographer> findPhotographerWithMoods(@Param("photographerId") Long photographerId);

    Boolean existsByMemberId(@Param("memberId") Long memberId);
}
