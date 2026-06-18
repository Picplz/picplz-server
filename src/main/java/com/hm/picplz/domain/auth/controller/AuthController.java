package com.hm.picplz.domain.auth.controller;

import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;
import com.hm.picplz.domain.member.domain.SocialProvider;
import jakarta.validation.Valid;
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

	@Operation(summary = "카카오 로그인")
	@PostMapping("/kakao")
	public AuthDto.LoginResponse kakaoLogin(@RequestBody AuthDto.KakaoTokenRequest request) {
		return authService.checkUser(SocialProvider.KAKAO, request.getAccessToken());
	}

	@Operation(summary = "애플 로그인")
	@PostMapping("/apple")
	public AuthDto.LoginResponse appleLogin(@RequestBody AuthDto.AppleTokenRequest request) {
		return authService.checkUser(SocialProvider.APPLE, request.getIdToken());
	}

	@Operation(summary = "임시 로그인", tags = "test")
	@PostMapping("/test")
	public AuthDto.LoginResponse testLogin(@RequestBody AuthDto.TestLogin request) {
		return authService.testLogin(request);
	}

	@Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스 토큰 및 리프레시 토큰을 재발급합니다.")
	@PostMapping("/refresh")
	public JwtTokenResponseDto refresh(@RequestBody @Valid AuthDto.RefreshRequest request) {
		return authService.refreshToken(request.getRefreshToken());
	}
}
