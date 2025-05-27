package com.hm.picplz.domain.auth.dto;

import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;
import com.hm.picplz.domain.member.domain.SocialProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Builder;
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
	public static class LoginResponse {
		@Schema(description = "회원 가입 여부", example = "true / false")
		private boolean isRegistered;
		@Schema(description = "소셜 로그인에서 제공하는 유저 식별용 code값. 회원 가입 시 필요", example = "0123456789")
		private String socialCode;
		@Schema(description = "간편 로그인 제공 업체", example = "kakao / apple")
		private SocialProvider socialProvider;
		@Schema(description = "픽플즈 accessToken")
		private JwtTokenResponseDto token;

		@Builder
		public LoginResponse(boolean isRegistered, String socialCode, SocialProvider socialProvider, JwtTokenResponseDto token) {
			this.isRegistered = isRegistered;
			this.socialCode = socialCode;
			this.socialProvider = socialProvider;
			this.token = token;
		}

	}

	@Data
	@NoArgsConstructor
	public static class KakaoUserInfo {
		private String socialCode;
		private String email;

		public static KakaoUserInfo of(String socialCode, String email) {
			KakaoUserInfo kakaoUserInfo = new KakaoUserInfo();
			kakaoUserInfo.socialCode = socialCode;
			kakaoUserInfo.email = email;
			return kakaoUserInfo;
		}
	}

	@Data
	@NoArgsConstructor
	public static class TestLogin {
		private String socialCode;
		private SocialProvider socialProvider;
	}
}
