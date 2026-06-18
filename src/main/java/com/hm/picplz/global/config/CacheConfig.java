package com.hm.picplz.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 캐시 설정
 *
 * 캐시 적용 영역:
 * 1. chatRooms: 채팅방 목록 (TTL: 5분)
 * 2. chatRoom: 채팅방 단건 (TTL: 10분)
 * 3. chatMessages: 메시지 목록 첫 페이지 (TTL: 2분)
 * 4. member: 회원 정보 (TTL: 10분)
 * 5. photographer: 작가 정보 (TTL: 10분)
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final RedisConnectionFactory connectionFactory;

    @Bean
    public CacheManager cacheManager() {
        // ObjectMapper 설정 (LocalDateTime 직렬화 지원)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
            objectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );

        // Redis Serializer 설정
        GenericJackson2JsonRedisSerializer serializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // 기본 캐시 설정
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // 기본 TTL: 10분
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            )
            .disableCachingNullValues();  // null 값은 캐시하지 않음

        // 캐시별 개별 설정
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("chatRooms",
                defaultConfig.entryTtl(Duration.ofMinutes(5)))  // 채팅방 목록: 5분
            .withCacheConfiguration("chatRoom",
                defaultConfig.entryTtl(Duration.ofMinutes(10))) // 채팅방 단건: 10분
            .withCacheConfiguration("chatMessages",
                defaultConfig.entryTtl(Duration.ofMinutes(2)))  // 메시지 목록: 2분 (짧은 TTL)
            .withCacheConfiguration("member",
                defaultConfig.entryTtl(Duration.ofMinutes(10))) // 회원 정보: 10분
            .withCacheConfiguration("photographer",
                defaultConfig.entryTtl(Duration.ofMinutes(10))) // 작가 정보: 10분
            .withCacheConfiguration("customer",
                defaultConfig.entryTtl(Duration.ofMinutes(10))) // 고객 정보: 10분
            .build();
    }
}
