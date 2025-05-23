package com.hm.picplz.domain.photographer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.photographer.domain.DefaultCamera;

public interface CameraRepository extends JpaRepository<DefaultCamera, Long> {
}
