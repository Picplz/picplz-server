package com.hm.picplz.domain.photographer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.photographer.domain.Career;

public interface CareerRepository extends JpaRepository<Career, Long> {
}
