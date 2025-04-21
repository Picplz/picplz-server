package com.hm.picplz.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hm.picplz.domain.auth.dto.AuthDto;
import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;
import com.hm.picplz.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name="Auth")
public class AuthController {
	private final AuthService authService;

	@PostMapping("/kakao")
	public JwtTokenResponseDto kakaoLogin(@RequestBody AuthDto.KakaoTokenRequest request) {
		return authService.getUserInfo(request.getAccessToken());
	}
}
