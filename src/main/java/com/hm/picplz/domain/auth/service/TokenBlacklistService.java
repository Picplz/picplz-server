package com.hm.picplz.domain.auth.service;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 토큰을 블랙리스트에 등록
     */
    public void blacklistToken(String token, long remainingMs) {
        stringRedisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "blocked",
                remainingMs,
                TimeUnit.MILLISECONDS
        );
        log.debug("토큰 블랙리스트 등록 완료 - 남은 유효시간: {}ms", remainingMs);
    }

    /**
     * 토큰이 블랙리스트에 등록되어 있는지 확인
     */
    public boolean isBlacklisted(String token) {
        // Redis에서 해당 키가 존재하는지 확인 (hasKey: 키 존재 여부만 체크)
        Boolean exists = stringRedisTemplate.hasKey(BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(exists);
    }
}
