package com.hm.picplz.domain.auth.dto;

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
	public static class KakaoUserInfo {
		private Long id;
		private String email;
		private String nickname;

		public static KakaoUserInfo of(Long id, String email, String nickname) {
			KakaoUserInfo kakaoUserInfo = new KakaoUserInfo();
			kakaoUserInfo.id = id;
			kakaoUserInfo.email = email;
			kakaoUserInfo.nickname = nickname;
			return kakaoUserInfo;
		}
	}
}
