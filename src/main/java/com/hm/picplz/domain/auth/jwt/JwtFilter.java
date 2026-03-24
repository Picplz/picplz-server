package com.hm.picplz.domain.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
	public static final String AUTHORIZATION_HEADER = "Authorization";
	private final JwtTokenProvider jwtTokenProvider;

	private final List<String> permitAllUrls; // 권한 검증 제외 경로
	private boolean isPermitAll(String uri) {
		return permitAllUrls.stream().anyMatch(uri::startsWith);
	}

	// 실제 필터링 로직
	// 토큰의 인증정보를 SecurityContext에 저장하는 역할 수행
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws IOException, ServletException {

		String jwt = resolveToken(request);
		String requestURI = request.getRequestURI();

		if (!isPermitAll(requestURI)) {
			if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
				Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("인증 성공 - 사용자: {}, URI: {}", authentication.getName(), requestURI);
			} else {
				log.debug("인증 실패 또는 토큰 없음 - URI: {}", requestURI);
			}
		}

		filterChain.doFilter(request, response);
	}

	// Request Header 에서 토큰 정보를 꺼내오기 위한 메소드
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return null;
	}

}