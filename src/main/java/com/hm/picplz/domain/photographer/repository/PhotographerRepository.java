package com.hm.picplz.domain.photographer.repository;

import com.hm.picplz.domain.photographer.domain.Photographer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotographerRepository extends JpaRepository<Photographer, Long> {
    @Query("SELECT p FROM Photographer p JOIN FETCH p.member WHERE p.member.id = :memberId")
    Optional<Photographer> findByMemberId(@Param("memberId") Long memberId);
}
