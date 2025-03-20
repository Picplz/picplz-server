package com.hm.picplz.domain.photographer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hm.picplz.domain.photographer.domain.Photographer;

@Repository
public interface PhotographerRepository extends JpaRepository<Photographer, Long> {
    @Query("SELECT p FROM Photographer p JOIN FETCH p.member WHERE p.member.id = :memberId")
    Optional<Photographer> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT p FROM Photographer p JOIN FETCH p.member LEFT JOIN FETCH p.photoMoods WHERE p.id = :photographerId")
    Optional<Photographer> findPhotographerWithMoods(@Param("photographerId") Long photographerId);
}
