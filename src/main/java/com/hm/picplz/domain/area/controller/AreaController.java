package com.hm.picplz.domain.area.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hm.picplz.domain.area.dto.AreaDto;
import com.hm.picplz.domain.area.service.AreaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/areas")
@Tag(name = "Area")
public class AreaController {

	private final AreaService areaService;

	@Operation(summary = "사용자 근처 법정동 반환")
	@GetMapping(value = "/nearby")
	public List<AreaDto.AreaInfo> getNearbyAreas(
		@RequestParam int rad, @RequestParam double lat, @RequestParam double lng) {
		return areaService.getNearbyAreas(rad, lat, lng);
	}

	@Operation(summary = "키워드로 법정동 검색")
	@GetMapping(value = "/search")
	public List<AreaDto.AreaInfo> searchAreasWithKeyword(@RequestParam String keyword) {
		return areaService.searchAreasWithKeyword(keyword);
	}
}
