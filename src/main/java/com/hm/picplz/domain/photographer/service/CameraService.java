package com.hm.picplz.domain.photographer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hm.picplz.domain.photographer.dto.CameraDto;
import com.hm.picplz.domain.photographer.repository.CameraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CameraService {
	private final CameraRepository cameraRepository;

	public List<CameraDto.DefaultCameraCard> getCameras() {
		return cameraRepository.findAll().stream().map(CameraDto.DefaultCameraCard::from).toList();
	}
}
