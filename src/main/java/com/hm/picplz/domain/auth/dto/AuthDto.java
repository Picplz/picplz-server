package com.hm.picplz.domain.auth.dto;

import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthDto {

	@Data
	@NoArgsConstructor
	public static class KakaoTokenRequest {
		private String accessToken;
	}

	@Data
	@NoArgsConstructor
	public static class LoginResult {
		@Schema(description = "회원 가입 여부", example = "true / false")
		private boolean isRegistered;
		@Schema(description = "간편 로그인 제공 업체", example = "kakao / apple")
		private String provider;
		@Schema(description = "픽플즈 accessToken")
		private JwtTokenResponseDto token;

		public static LoginResult of(boolean isRegistered, String provider, JwtTokenResponseDto token) {
			LoginResult loginResult = new LoginResult();
			loginResult.isRegistered = isRegistered;
			loginResult.provider = provider;
			loginResult.token = token;
			return loginResult;
		}
	}

	@Data
	@NoArgsConstructor
	public static class KakaoUserInfo {
		private Long id;
		private String email;

		public static KakaoUserInfo of(Long id, String email) {
			KakaoUserInfo kakaoUserInfo = new KakaoUserInfo();
			kakaoUserInfo.id = id;
			kakaoUserInfo.email = email;
			return kakaoUserInfo;
		}
	}
}
