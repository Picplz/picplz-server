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
import com.hm.picplz.domain.member.repository.MemberRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.SocialProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;
	private final RestClient restClient = RestClient.create();

	public AuthDto.LoginResponse checkUserKakao(String accessToken) {
		AuthDto.KakaoUserInfo userInfo = fetchKakaoUserInfo(accessToken);
		Optional<Member> member = findByCodeAndProvider(String.valueOf(userInfo.getId()), SocialProvider.KAKAO);
		return member.map(
				value -> AuthDto.LoginResponse.of(true, SocialProvider.KAKAO.getName(), generateTokenForMember(value)))
			.orElseGet(() -> AuthDto.LoginResponse.of(false, SocialProvider.KAKAO.getName(), null));
	}

	private AuthDto.KakaoUserInfo fetchKakaoUserInfo(String accessToken) {
		Map<String, Object> body = restClient.get()
			.uri("https://kapi.kakao.com/v2/user/me")
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.body(Map.class);

		Long id = ((Number) body.get("id")).longValue(); // 필수 값
		// kakao email은 not null이어야하는데 카카오 앱이 비즈앱이 아니라 email을 강제로 받을 수 없음
		Map<String, Object> account = Optional.ofNullable((Map<String, Object>) body.get("kakao_account"))
			.orElse(Collections.emptyMap());
		String email = (String) account.get("email");
		return AuthDto.KakaoUserInfo.of(id, email);
	}

	private Optional<Member> findByCodeAndProvider(String code, SocialProvider socialProvider) {
		return memberRepository.findByAttributeCodeAndSocialProvider(code, socialProvider);
	}

	private JwtTokenResponseDto generateTokenForMember(Member member) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			member.getId(), null,
			List.of(new SimpleGrantedAuthority(member.getRoleKey()))
		);
		return jwtTokenProvider.generateTokenDto(authentication);
	}
}