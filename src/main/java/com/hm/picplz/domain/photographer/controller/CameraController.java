package com.hm.picplz.domain.photographer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hm.picplz.domain.photographer.dto.CameraDto;
import com.hm.picplz.domain.photographer.service.CameraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/cameras")
@Tag(name = "Camera")
public class CameraController {

	private final CameraService cameraService;

	@Operation(summary = "기획서에서 정한 기본 촬영 기기 목록 반환")
	@GetMapping
	public List<CameraDto.DefaultCameraCard> getAllCameras() {
		return cameraService.getCameras();
	}
}
