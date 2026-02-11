# picplz-server

사진작가와 고객을 연결하는 촬영 예약 플랫폼 백엔드 서버

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.2 |
| Build | Gradle |
| Database | MySQL, Redis |
| Auth | JWT, OAuth 2.0 (Kakao, Apple) |
| Storage | AWS S3 |
| Docs | Swagger (SpringDoc OpenAPI) |

## 주요 기능

- 소셜 로그인 (카카오, 애플)
- 사진작가 프로필 및 포트폴리오 관리
- 촬영 상품(패키지) 등록/관리
- 예약 및 결제 처리
- 리뷰/평점 시스템
- 팔로우 시스템
- S3 Presigned URL 이미지 업로드

## 프로젝트 구조

```
src/main/java/com/hm/picplz/
├── domain/                 # 비즈니스 도메인
│   ├── auth/              # 인증/JWT
│   ├── member/            # 회원
│   ├── photographer/      # 사진작가
│   ├── customer/          # 고객
│   ├── product/           # 촬영 상품
│   ├── portfolio/         # 포트폴리오
│   ├── reservation/       # 예약
│   ├── review/            # 리뷰
│   ├── following/         # 팔로우
│   └── area/              # 지역 정보
├── global/                # 전역 설정
│   ├── config/            # 설정 (Security, Redis, Swagger)
│   ├── common/            # 공통 컴포넌트
│   └── error/             # 에러 처리
└── infra/                 # 인프라
    └── s3/                # AWS S3 연동
```

## 로컬 실행

### 사전 요구사항

- JDK 17
- MySQL 8.0+
- Redis

### 환경 변수 설정

`src/main/resources/application-dev.yml` 노션을 참고하여 시크릿 설정:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/picplz?serverTimezone=Asia/Seoul
    username: {DB_USERNAME}
    password: {DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect
  data:
    redis:
      host: localhost
      port: 6379

# JWT 설정
jwt:
  secret: {JWT_SECRET_KEY}
  access-token-validity: 3600000
  refresh-token-validity: 604800000

# OAuth 설정
oauth:
  kakao:
    client-id: {KAKAO_CLIENT_ID}
    client-secret: {KAKAO_CLIENT_SECRET}
  apple:
    client-id: {APPLE_CLIENT_ID}
    team-id: {APPLE_TEAM_ID}
    key-id: {APPLE_KEY_ID}

# AWS S3 설정
cloud:
  aws:
    s3:
      bucket: {S3_BUCKET_NAME}
    credentials:
      access-key: {AWS_ACCESS_KEY}
      secret-key: {AWS_SECRET_KEY}
    region:
      static: ap-northeast-2
```

## Docker 배포

### 로컬 실행 Docker Compose

```
미리 mySql 실행필요
```

`docker-compose.yml` 예시:

```yaml
version: '3.8'
services:
   spring-app:
      container_name: picplz-dev-server
      ports:
         - "8080:8080"
      build:
         context: .
         dockerfile: Dockerfile.dev  # 개발용
      volumes:
         # 로컬에서 Gradle로 빌드한 jar 파일을 컨테이너의 app.jar에 덮어씌워서 변경 사항 반영
         - ./build/libs/picplz-0.0.1-SNAPSHOT.jar:/picplz/app.jar
      depends_on:
         - redis
   redis:
      image: redis:7.0
      container_name: redis
      ports:
         - "6379:6379"
      volumes:
         - redis-data:/data
      environment:
         REDIS_PASSWORD: picplz2025!!
      networks:
         - default

volumes:
   redis-data:

networks:
   default:
      driver: host
```

```bash
docker compose up -d
```

## CI/CD

GitHub Actions를 통해 자동 빌드 및 배포가 구성되어 있습니다.

### 워크플로우

1. **빌드** (`develop` 브랜치 push/PR 시)
   - JDK 17 설정
   - Gradle 빌드 실행

2. **배포** (`develop` 브랜치 push 시)
   - application-dev.yml 생성 (GitHub Secrets 사용)
   - Docker 이미지 빌드 및 Docker Hub push
   - EC2 서버에 SSH 접속하여 배포

### 필요한 GitHub Secrets

| Secret | 설명 |
|--------|------|
| `DEV` | application-dev.yml 전체 내용 |
| `DOCKER_USERNAME` | Docker Hub 사용자명 |
| `DOCKER_TOKEN` | Docker Hub 액세스 토큰 |
| `DOCKER_REPO` | Docker Hub 저장소명 |
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_PEM_KEY` | EC2 SSH 프라이빗 키 |

## API 엔드포인트

| 경로 | 설명 |
|------|------|
| `/api/v1/auth` | 인증 (로그인/토큰) |
| `/api/v1/member` | 회원 정보 |
| `/api/v1/photographer` | 사진작가 |
| `/api/v1/product` | 촬영 상품 |
| `/api/v1/portfolio` | 포트폴리오 |
| `/api/v1/area` | 지역 정보 |
| `/api/v1/s3` | 이미지 업로드 |


---
