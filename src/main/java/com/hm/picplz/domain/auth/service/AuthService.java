package com.hm.picplz.domain.auth.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hm.picplz.domain.auth.dto.AuthDto;
import com.hm.picplz.domain.auth.jwt.JwtTokenProvider;
import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.SocialProvider;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.member.repository.MemberRepository;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("SameParameterValue")
@Service
@RequiredArgsConstructor
public class AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final MemberRepository memberRepository;
	private final RestClient restClient = RestClient.create();

	public AuthDto.LoginResponse checkUserKakao(String accessToken) {
		AuthDto.KakaoUserInfo userInfo = fetchKakaoUserInfo(accessToken);
		Optional<Member> member = findByCodeAndProvider(String.valueOf(userInfo.getSocialCode()), SocialProvider.KAKAO);
		return member.map(
				value -> AuthDto.LoginResponse.builder()
					.isRegistered(true)
					.socialCode(userInfo.getSocialCode())
					.socialProvider(SocialProvider.KAKAO)
					.token(generateTokenForMember(value))
					.build())
			.orElseGet(() -> AuthDto.LoginResponse.builder()
				.isRegistered(false)
				.socialCode(userInfo.getSocialCode())
				.socialProvider(SocialProvider.KAKAO)
				.build());
	}

	private AuthDto.KakaoUserInfo fetchKakaoUserInfo(String accessToken) {
		ParameterizedTypeReference<Map<String, Object>> responseType =
			new ParameterizedTypeReference<>() {};

		Map<String, Object> body = restClient.get()
			.uri("https://kapi.kakao.com/v2/user/me")
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.body(responseType);

		if (body == null) {
			throw ExceptionFactory.of(MemberErrorCode.NO_KAKAO_USER);
		}
		String code = (String) body.get("id"); // 필수 값

		// 카카오 앱이 비즈앱이 아니라 email 수집을 강제할 수 없어, email 파싱 코드는 생략했습니다.
		// (비즈앱 전환 후 아래 로직 참고: kakao_account에서 email 가져오기)
		// 예시 (비즈앱 전환 시 사용):
		// "Map<String, Object> account = Optional.ofNullable((Map<String, Object>) body.get(\"kakao_account\"))"
		// + ".orElse(Collections.emptyMap()); String email = (String) account.get(\"email\");"
		return AuthDto.KakaoUserInfo.of(code, null);
	}

	private Optional<Member> findByCodeAndProvider(String code, SocialProvider socialProvider) {
		return memberRepository.findBySocialCodeAndSocialProvider(code, socialProvider);
	}

	private JwtTokenResponseDto generateTokenForMember(Member member) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			member.getId(), null,
			List.of(new SimpleGrantedAuthority(member.getRoleKey()))
		);
		return jwtTokenProvider.generateTokenDto(authentication);
	}

	public AuthDto.LoginResponse testLogin(AuthDto.TestLogin req) {
		return findByCodeAndProvider(String.valueOf(req.getSocialCode()), req.getSocialProvider())
			.map(value -> AuthDto.LoginResponse.builder()
				.isRegistered(true)
				.socialCode(value.getSocialCode())
				.socialProvider(value.getSocialProvider())
				.token(generateTokenForMember(value))
				.build())
			.orElseGet(() -> AuthDto.LoginResponse.builder()
				.isRegistered(false)
				.build());
	}
}