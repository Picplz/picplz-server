package com.hm.picplz.domain.photographer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.photographer.domain.ActiveArea;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActiveAreaRepository extends JpaRepository<ActiveArea, Long> {

    @Query("""
    SELECT aa 
    FROM ActiveArea aa 
    JOIN FETCH aa.photographer 
    WHERE aa.area.id = :areaId
    """)
    List<ActiveArea> findAllByAreaIdWithPhotographer(Long areaId);
}
