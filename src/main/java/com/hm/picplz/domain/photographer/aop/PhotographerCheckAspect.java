package com.hm.picplz.domain.photographer.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.service.PhotographerService;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class PhotographerCheckAspect {

	private final PhotographerService photographerService;

	@Before("@annotation(com.hm.picplz.domain.photographer.annotation.PhotographerOnly)")
	public void checkPhotographer(JoinPoint joinPoint) {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long memberId = (Long) principal;
		boolean isPhotographer = photographerService.checkAndCachePhotographer(memberId);
		if (!isPhotographer) {
			throw ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND);
		}
	}
}