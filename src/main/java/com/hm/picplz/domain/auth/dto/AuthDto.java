package com.hm.picplz.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.domain.SocialProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthDto {

	@Data
	@NoArgsConstructor
	public static class RefreshRequest {
		@NotBlank
		private String refreshToken;
	}

	@Data
	@NoArgsConstructor
	public static class KakaoTokenRequest {
		private String accessToken;
	}

	@Data
	@NoArgsConstructor
	public static class AppleTokenRequest {
		private String idToken;
	}

	@Data
	@NoArgsConstructor
	public static class LoginResponse {
		@Schema(description = "회원 가입 여부", example = "true / false")
		private boolean isRegistered;
		@Schema(description = "소셜 로그인에서 제공하는 유저 식별용 code값. 회원 가입 시 필요", example = "0123456789")
		private String socialCode;
		@Schema(description = "소셜 로그인에서 제공하는 유저 email", example = "picplz@ppz.com")
		private String socialEmail;
		@Schema(description = "간편 로그인 제공 업체", example = "kakao / apple")
		private SocialProvider socialProvider;
		@Schema(description = "픽플즈 accessToken")
		private JwtTokenResponseDto token;
		@Schema(description = "현재 역할")
		private Role currentRole;

		@Builder
		public LoginResponse(boolean isRegistered, String socialCode, String socialEmail, SocialProvider socialProvider,
				JwtTokenResponseDto token, Role currentRole) {
			this.isRegistered = isRegistered;
			this.socialCode = socialCode;
			this.socialEmail = socialEmail;
			this.socialProvider = socialProvider;
			this.token = token;
			this.currentRole = currentRole;
		}

	}

	@Data
	@NoArgsConstructor
	public static class KakaoUserInfo implements SocialUserInfo {
		private SocialProvider socialProvider;
		private String socialCode;
		private String socialEmail;

		public static KakaoUserInfo of(String socialCode, String email) {
			KakaoUserInfo kakaoUserInfo = new KakaoUserInfo();
			kakaoUserInfo.socialProvider = SocialProvider.KAKAO;
			kakaoUserInfo.socialCode = socialCode;
			kakaoUserInfo.socialEmail = email;
			return kakaoUserInfo;
		}
	}

	@Data
	@NoArgsConstructor
	public static class TestLogin {
		private String socialCode;
		private SocialProvider socialProvider;

	}

	@Data
	public static class ApplePublicKeys {

		@JsonProperty("keys")
		private List<ApplePublicKey> keys;

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		public static class ApplePublicKey {
			private String kty;
			private String kid;
			private String use;
			private String alg;
			private String n;
			private String e;
		}
	}

	@Data
	public static class AppleUserInfo implements SocialUserInfo{
		private SocialProvider socialProvider;
		private String socialCode;
		private String socialEmail;

		public static AppleUserInfo of(String socialCode, String email) {
			AppleUserInfo appleUserInfo = new AppleUserInfo();
			appleUserInfo.socialProvider = SocialProvider.APPLE;
			appleUserInfo.socialCode = socialCode;
			appleUserInfo.socialEmail = email;
			return appleUserInfo;
		}
	}
}
