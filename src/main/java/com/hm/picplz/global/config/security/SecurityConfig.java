package com.hm.picplz.global.config.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hm.picplz.domain.auth.jwt.JwtAccessDeniedHandler;
import com.hm.picplz.domain.auth.jwt.JwtAuthenticationEntryPoint;
import com.hm.picplz.domain.auth.jwt.JwtFilter;
import com.hm.picplz.domain.auth.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	private final JwtTokenProvider jwtTokenProvider;

	/* 권한 제외 대상 */
	private final List<String> permitAllUrls; // 주입

	/* Admin 접근 권한 */
	private static final String[] permitAdminUrl = new String[] {
	};

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
		throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(CorsConfig.corsConfigurationSource()))
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)
			.addFilterBefore(new JwtFilter(jwtTokenProvider, permitAllUrls), UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(authz -> authz
				.requestMatchers(permitAllUrls.toArray(String[]::new)).permitAll()
				.requestMatchers(permitAdminUrl).hasRole("ADMIN")
				.anyRequest().authenticated()
			)
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
