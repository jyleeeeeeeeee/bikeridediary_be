# 바라다 (BikeRideDiary) — Backend API

바이크 라이더를 위한 정비 기록 및 라이딩 코스 관리 앱의 Spring Boot 백엔드 서버

## 기술 스택

- Java 21
- Spring Boot 3.3
- PostgreSQL 16 + PostGIS 3.4
- Redis 7
- AWS S3
- JWT + OAuth2 (Kakao, Google, Apple)

## 로컬 개발 환경 시작

### 1. Docker로 DB 실행

```bash
cd docker
docker-compose up -d
```

### 2. 환경변수 설정

`src/main/resources/application-local.yml` 파일에서 필요한 API Key 설정
(실제 외부 API 테스트가 필요한 경우에만)

### 3. Spring Boot 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 4. Swagger UI 확인

http://localhost:8080/swagger-ui.html

## 패키지 구조

```
com.bikeridediary
├── domain
│   ├── user          - 사용자
│   ├── bike          - 바이크
│   ├── bikemodel     - 제조사/모델 마스터 (2차)
│   ├── maintenance   - 정비 이력
│   ├── fueling       - 연비/주유
│   ├── course        - 라이딩 코스
│   ├── station       - 주유소 검색
│   └── document      - 서류/사진
├── global
│   ├── auth          - JWT, OAuth2, Apple Sign In
│   ├── config        - Security, Swagger, Redis 설정
│   ├── exception     - 예외 처리
│   └── response      - 공통 응답 포맷
└── infra
    ├── s3            - AWS S3 파일 업로드
    ├── fcm           - Firebase 푸시 알림
    ├── weather       - OpenWeather API
    ├── naver         - 네이버 Directions / Geocoding
    ├── kakao         - 카카오 로컬 API
    └── opinet        - 오피넷 유가 API
```

## API 버전

모든 API는 `/api/v1/` prefix 사용

## 개발 규칙

- 모든 주석, 변수명, 커밋 메시지는 영어로 작성
- 응답은 ApiResponse 래퍼 사용
- 예외는 BusinessException + ErrorCode로 처리
- 테스트: JUnit5 + Mockito
