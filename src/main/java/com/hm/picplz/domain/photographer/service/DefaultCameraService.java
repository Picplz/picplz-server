package com.hm.picplz.domain.photographer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hm.picplz.domain.photographer.dto.DefaultCameraDto;
import com.hm.picplz.domain.photographer.repository.DefaultCameraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultCameraService {
	private final DefaultCameraRepository defaultCameraRepository;

	public List<DefaultCameraDto.CameraInfo> getCameras() {
		return defaultCameraRepository.findAll().stream().map(DefaultCameraDto.CameraInfo::from).toList();
	}
}
