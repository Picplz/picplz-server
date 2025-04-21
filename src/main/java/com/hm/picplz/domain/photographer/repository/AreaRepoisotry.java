package com.hm.picplz.domain.photographer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.area.domain.Area;

public interface AreaRepoisotry extends JpaRepository<Area, Long> {
}
