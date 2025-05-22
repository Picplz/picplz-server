package com.hm.picplz.domain.photographer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.photographer.domain.PhotographerCamera;

public interface PhotographerCameraRepository extends JpaRepository<PhotographerCamera, Long> {
}
