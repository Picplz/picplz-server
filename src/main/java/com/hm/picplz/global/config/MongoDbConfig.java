package com.hm.picplz.global.config;

import com.hm.picplz.domain.chat.repository.ChatMessageRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB 설정
 *
 * JPA와 MongoDB를 함께 사용하기 위한 MongoDB Repository 스캔 설정
 * basePackageClasses를 사용하여 패키지명 하드코딩을 방지하고
 * 패키지 구조 변경에 자동 대응
 */
@Configuration
@EnableMongoRepositories(
    basePackageClasses = ChatMessageRepository.class
)
public class MongoDbConfig {
    // MongoDB Repository 스캔 기준점으로 ChatMessageRepository 사용
    // 해당 클래스가 속한 패키지와 하위 패키지의 MongoRepository를 모두 스캔
}
