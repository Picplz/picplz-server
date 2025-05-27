package com.hm.picplz.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hm.picplz.domain.auth.dto.AuthDto;
import com.hm.picplz.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
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

	@Operation(summary = "회원인 경우 픽플즈 토큰 반환, 아닐 경우 ")
	@PostMapping("/kakao")
	public AuthDto.LoginResponse kakaoLogin(@RequestBody AuthDto.KakaoTokenRequest request) {
		return authService.checkUserKakao(request.getAccessToken());
	}

	@Operation(summary = "임시 로그인", tags = "test")
	@PostMapping("/test")
	public AuthDto.LoginResponse testLogin(@RequestBody AuthDto.TestLogin request) {
		return authService.testLogin(request);
	}
}
