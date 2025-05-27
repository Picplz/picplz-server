package com.hm.picplz.global.config.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermitAllConfig {

	@Bean
	public List<String> permitAllUrls() {
		return List.of(
			"/swagger-ui/**",
			"/swagger-resources/**",
			"/v3/api-docs/**",      // 하위 경로 모두 포함
			"/webjars/**",
			"/members/test", "/members/nickname",
			"/auth/**",
			"/cameras",
			"/photographers", // 작가 회원가입
			"/customers", // 고객 회원가입
			"/s3/**" // s3
			// , "/**" // 개발용 검증 해제
		);
	}
}