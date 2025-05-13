package com.hm.picplz.domain.auth.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hm.picplz.domain.auth.dto.AuthDto;
import com.hm.picplz.domain.auth.jwt.JwtTokenProvider;
import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;
import com.hm.picplz.domain.member.MemberRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;
	private final RestClient restClient = RestClient.create();

	public JwtTokenResponseDto getUserInfo(String accessToken) {
		AuthDto.KakaoUserInfo userInfo = fetchKakaoUserInfo(accessToken);
		Member member = findOrCreateMember(userInfo);
		return generateTokenForMember(member);
	}

	private AuthDto.KakaoUserInfo fetchKakaoUserInfo(String accessToken) {
		Map<String, Object> body = restClient.get()
			.uri("https://kapi.kakao.com/v2/user/me")
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.body(Map.class);

		Long id = ((Number) body.get("id")).longValue(); // 필수 값
		// 필수가 아닌 값들
		Map<String, Object> account = Optional.ofNullable((Map<String, Object>) body.get("kakao_account"))
			.orElse(Collections.emptyMap());

		Map<String, Object> profile = Optional.ofNullable((Map<String, Object>) account.get("profile"))
			.orElse(Collections.emptyMap());
		// kakao email은 not null이어야하는데 카카오 앱이 비즈앱이 아니라 email, 닉네임을 강제로 받을 수 없음
		String email = Optional.ofNullable((String) account.get("email")).orElse(null);
		String nickname = Optional.ofNullable((String) profile.get("nickname")).orElse("게스트");

		return AuthDto.KakaoUserInfo.of(id, email, nickname);
	}

	private Member findOrCreateMember(AuthDto.KakaoUserInfo userInfo) {
		return memberRepository.findByAttributeCode(String.valueOf(userInfo.getId()))
			.orElseGet(() -> memberRepository.save(Member.builder()
				.name(userInfo.getNickname())
				.kakaoEmail(userInfo.getEmail())
				.attributeCode(String.valueOf(userInfo.getId()))
				.provider("kakao")
				.role(Role.GENERAL)
				.build()));
	}

	private JwtTokenResponseDto generateTokenForMember(Member member) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			member.getId(), null,
			List.of(new SimpleGrantedAuthority(member.getRoleKey()))
		);
		return jwtTokenProvider.generateTokenDto(authentication);
	}
}