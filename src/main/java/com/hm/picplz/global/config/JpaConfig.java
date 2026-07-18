package com.hm.picplz.global.config;

import com.hm.picplz.PicplzApplication;
import com.hm.picplz.domain.chat.repository.ChatMessageRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 설정
 *
 * MongoDB와 JPA를 함께 사용하기 위한 JPA Repository 스캔 설정
 * MongoDB Repository를 제외하고 나머지 모든 JPA Repository를 스캔
 *
 * basePackageClasses를 사용하여 애플리케이션 루트 패키지를 기준으로 스캔하되,
 * MongoRepository는 명시적으로 제외하여 Bean 등록 충돌 방지
 */
@Configuration
@EnableJpaRepositories(
    basePackageClasses = PicplzApplication.class,
    excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
        classes = ChatMessageRepository.class
    )
)
public class JpaConfig {
    // PicplzApplication 클래스가 속한 패키지(com.hm.picplz)와 하위 패키지의
    // JpaRepository를 스캔하되, ChatMessageRepository는 제외
}
