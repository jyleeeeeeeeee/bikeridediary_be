# 프로젝트 컨텍스트 — 바라다 (BikeRideDiary)

## 나에 대해

- 직업: 프리랜서 백엔드 개발자
- 주력 스택: Java, Spring Boot
- 프론트엔드 경험: HTML, CSS, JS, jQuery, Thymeleaf 수준
- 취미: 바이크 라이딩

---

## 프로젝트 개요

**서비스명 (한글)**: 바라다
**서비스명 (영문)**: BikeRideDiary
**약칭**: BRD
**도메인 후보**: barada.kr / barada.app
**패키지**: com.bikeridediary

바이크 라이더를 위한 풀스택 앱. 정비/소모품 교체 이력 관리와 라이딩 코스 기록·공유가 핵심 기능이다.
개인 도구로 시작해서 공개 커뮤니티 서비스로 성장시키는 것이 목표다.

슬로건 후보: 달리고, 기록하고, 바라다

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 백엔드 | Spring Boot 3.x, Spring Security, Spring Data JPA, QueryDSL |
| 인증 | JWT + OAuth2 (카카오, 구글, Apple) |
| DB | PostgreSQL + PostGIS 확장 |
| 캐시 | Redis |
| 스토리지 | AWS S3 (GPX 파일, 이미지) |
| 웹 프론트 | 추후 검토 — 앱 출시 후 커뮤니티 성장 시 Vue 3 도입 예정 |
| 모바일 앱 | Flutter (Dart) — flutter_naver_map SDK, flutter_foreground_task ← 1순위 |
| 인프라 | Docker Compose, GitHub Actions, AWS |
| API 문서 | Swagger / OpenAPI 3.0 |

---

## 핵심 도메인

**바이크 (Bike)**
- 사용자가 여러 대의 바이크를 등록할 수 있음
- 제조사, 모델명, 연식, 카테고리, 총 주행거리 관리
- 제조사/모델 입력 방식은 단계별로 전환
  - MVP: 텍스트 직접 입력 (빠른 개발 + 사용 데이터 수집)
  - 2차: 사용 빈도 기반 DB 구축 (manufacturers / bike_models / bike_trims 테이블)
  - 3차: 자동완성 + 선택 UI, DB 없는 모델은 직접 입력 유지

**정비 이력 (Maintenance)**
- 소모품 교체 기록 (엔진오일, 타이어, 체인, 브레이크패드 등)
- 교체 당시 주행거리, 날짜, 메모, 비용
- 교체 주기 설정 (km 기준 또는 날짜 기준)
- 다음 교체 예정 알림

**라이딩 코스 (Riding Course)**
- 세 가지 방식으로 코스 생성 (아래 코스 생성 섹션 참고)
- 거리, 소요 시간, 평균 속도, 고도, 난이도
- 공개/비공개 설정
- 공개 코스는 다른 라이더가 탐색·저장 가능
- 좋아요, 댓글

**사용자 (User)**
- 소셜 로그인 전용 (이메일/비밀번호 로그인 없음)
- Apple 로그인 시 이메일 숨김 처리 필요 (가상 이메일 저장)

---

## 기능 목록

### 정비 관리
- 소모품 교체 기록 (엔진오일, 타이어, 체인, 브레이크패드 등) + 비용 기록
- 교체 주기 설정 (km 기준 / 날짜 기준) 및 다음 교체 예정 알림
- 연비 계산 (주유량 + 주행거리 입력 → 자동 계산 및 추이 기록)
- 주요 서류 및 정비 이력서 사진 기록 (S3 업로드)
- 보험 만료 알림
- 정기 검사 알림

### 코스 생성 (3가지 방식)
- GPX 파일 직접 업로드 (Strava, Komoot 등 외부 앱에서 가져오기)
- 라이딩 중 GPS 자동 기록 → 종료 후 코스로 저장
- 지도에서 직접 생성
  - flutter_naver_map SDK로 앱 내 지도 표시
  - 출발지 / 경유지(최대 15개) / 목적지 텍스트 입력
  - 네이버 Geocoding API로 주소 → 좌표 변환
  - Spring Boot 서버가 네이버 Directions 15 API 호출 (API Key 서버에서 관리)
  - 응답의 path 좌표 배열을 NPolylineOverlay로 지도에 표시
  - 확인 후 저장 시 path 좌표를 GPX로 변환하여 S3 저장

### 주유소 검색
- 현재 위치 기반 근처 주유소 검색
  - 필터: 현재 영업중 / 24시 영업, 셀프 주유 여부, 고급유/일반유 가능 여부
  - 카카오 로컬 API + 오피넷(한국석유공사) API 연동
- 라이딩 코스 경로 근처 주유소 검색
  - GPX 경로에서 일정 간격으로 포인트 샘플링 후 PostGIS 반경 검색

### 라이딩 기록 및 분석
- 라이딩 시 날씨/기온 자동 기록 (OpenWeather API 연동)
- 코스 분석: 고도, 총 거리, 평균 속도, 난이도 자동 태깅
- 통계: 월별 주행거리, 평균 속도, 누적 라이딩 횟수/거리
- 코스 저장 / 즐겨찾기
- 공개 코스 탐색, 좋아요, 댓글

### 라이딩 퍼포먼스 (Flutter 앱 전용, 센서 기반)
- 공통 사항
  - sensors_plus 패키지로 자이로스코프 + 가속도계 데이터 수신
  - 저역통과 필터(Low-pass filter)로 엔진 진동 노이즈 제거
  - 참고용 수치임을 UI에 명시 (BMW IMU 같은 전용 하드웨어 수준 아님)

- 뱅킹각 측정 (롤축)
  - 거치 상태에서 캘리브레이션 → 현재 롤값을 0으로 초기화
  - 오르막/내리막 경사는 피치축 영향이라 롤축 측정에 영향 미미 (오차 2~5도 수준)
  - 와인딩 코너에서도 측정 가능

- 윌리 각도 측정 (피치축)
  - 평지에서만 측정 가능 — 경사로에서는 도로 경사값이 피치값에 섞여 분리 불가
  - GPS로 실시간 경사도 계산, 일정 기준 이상이면 "측정 불가 구간" 표시 후 비활성화
  - 평지 직선 구간에서만 동작

- 라이딩 후 퍼포먼스 요약
  - 최대 뱅킹각, 최대 윌리 각도, 급가속/급제동 횟수 등 통계 표시

### 기능 우선순위
- MVP (1차): 소모품 교체 기록/비용/알림, 연비 계산, 보험/검사 알림, GPS 라이딩 기록, 날씨 기록, 월별 통계
- 2차: 코스 지도 생성, GPX 업로드, 주유소 검색, 코스 분석/난이도, 사진 기록, 코스 즐겨찾기
- 3차: 뱅킹각/윌리 측정, 코스 근처 주유소, 커뮤니티 기능 (팔로우, 같이 라이딩 모집 등)

---

## 외부 API 연동

| API | 용도 | 비고 |
|-----|------|------|
| 네이버 Directions 15 | 코스 생성 경로 계산 (경유지 최대 15개) | API Key 서버에서 관리 |
| 네이버 Geocoding | 주소 텍스트 → 좌표 변환 | Directions API 연동 전처리 |
| flutter_naver_map SDK | 앱 내 지도 표시, 경로 Polyline | 네이버 클라우드 Client ID 필요 |
| 카카오 로컬 API | 주유소 검색 | 국내 데이터 커버리지 우수 |
| 오피넷 API | 주유소 유가 / 상세 정보 | 한국석유공사 공공 API |
| OpenWeather API | 라이딩 시 날씨/기온 기록 | 무료 플랜으로 충분 |
| Firebase FCM | 푸시 알림 (보험/검사/소모품) | Flutter + Spring Boot 연동 |
| Apple Sign In | iOS 소셜 로그인 필수 | identity_token JWT 검증 방식 |

---

## 인증 구조

- OAuth2로 카카오/구글/Apple 로그인 처리
- 로그인 완료 후 자체 JWT 발급 (Access Token + Refresh Token)
- Apple은 identity_token(JWT)을 Apple 공개키로 직접 검증하는 방식 (액세스토큰 방식 아님)
- Apple은 최초 1회만 유저 정보 제공 → 첫 응답 때 반드시 저장

---

## 코드 컨벤션 및 선호사항

- 언어: 모든 주석, 변수명, 문서는 **영어**로 작성 (커밋 메시지 포함)
- 응답 포맷: 공통 ApiResponse 래퍼 사용
- 예외 처리: GlobalExceptionHandler로 중앙 처리
- API 버전: `/api/v1/` prefix 사용
- 테스트: JUnit5 + Mockito (단위 테스트 우선)
- 패키지 구조: 도메인 기반 레이어드 아키텍처 (com.bikeridediary)
- Entity 클래스명: 반드시 "Entity" suffix 붙임 (User → UserEntity, Bike → BikeEntity)
- Entity 필드 주석: 모든 필드에 한글 주석으로 설명 작성 (예: `// 제조사명 (MVP: 텍스트 직접 입력)`)
- JPA dirty checking 활용: `@Transactional` 내에서 update/delete 시 `repository.save()` 호출하지 않음

```
com.bikeridediary
├── domain
│   ├── userEntity
│   ├── bikeEntity
│   ├── maintenance
│   ├── course
│   ├── fueling        ← 연비/주유 기록
│   ├── station        ← 주유소 검색
│   ├── document       ← 서류/사진 기록
│   └── bikemodel      ← 제조사/모델 마스터 데이터
├── global
│   ├── auth
│   ├── config
│   ├── exception
│   └── response
└── infra
    ├── s3
    ├── fcm
    ├── weather        ← OpenWeather API
    ├── naver          ← 네이버 Directions / Geocoding API
    ├── kakao          ← 카카오 로컬 API
    └── opinet         ← 오피넷 API
```

---

## 현재 개발 단계

### 완료된 작업

1. 기능 명세서 작성
2. ERD 설계
3. Spring Boot 프로젝트 초기 세팅
4. 인증 모듈 구현 (JWT + OAuth2 카카오/네이버, 이메일 로그인)
5. 사용자(User) 도메인 구현
6. 바이크(Bike) 도메인 구현
7. 정비(Maintenance) 도메인 구현
   - Entity: MaintenanceEntity, MaintenanceScheduleEntity, MaintenanceType(15종)
   - BaseEntity 추상 클래스 (공통 audit 필드)
   - Repository, DTO, Service, Controller 전체 구현
   - 12개 파일 코드 검토 완료 (2026-06-02)
8. CustomUserDetails 도입 — Controller에서 `userDetails.getUserId()`로 UUID 직접 접근
9. MaintenanceScheduleEntity.update()에서 maintenanceType 파라미터 제거 — 스케줄 정비 종류는 고정
10. 정비 도메인 단위 테스트 작성 완료 (2026-06-04)
    - MaintenanceServiceTest: 12개 테스트 (CRUD + 권한 검증)
    - MaintenanceScheduleServiceTest: 16개 테스트 (CRUD + 권한 검증 + 중복 스케줄 방지)
11. SQL 스키마 작성 완료 (2026-06-05)
    - 4개 테이블: users, bikes, maintenances, maintenance_schedules
    - 복합 인덱스 6개 (Repository 쿼리 메서드 기반)
    - RefreshToken은 Redis — PostgreSQL 스키마 미포함
12. OpenAPI 3.0 어노테이션 추가 완료 (2026-06-05)
    - SwaggerConfig: JWT Bearer 인증 스키마 설정 (기존)
    - 4개 컨트롤러에 @Tag, @Operation 어노테이션 추가 (21개 엔드포인트)
    - 접근 경로: http://localhost:8080/swagger-ui.html
13. 보류 사항 해결 (2026-06-05)
    - @ValidScheduleInterval 커스텀 검증: intervalKm/intervalMonths 둘 다 null 방지
    - MaintenanceResponse, MaintenanceScheduleResponse에 updatedAt 필드 추가
14. Docker 설정 보완 (2026-06-05)
    - Dockerfile (JRE Alpine 기반) + .dockerignore 추가
    - docker-compose.yml에 app 서비스 추가 (postgres/redis 의존)
    - 02_schema.sql init 스크립트로 테이블 자동 생성
15. GitHub Actions CI 파이프라인 추가 (2026-06-05)
    - push/PR 시 빌드 + 테스트 자동 실행
    - Gradle 캐싱, 테스트 리포트 artifact 업로드
16. 버그 수정 및 안정화 (2026-06-08)
    - BikeRepository JPA 쿼리 파생 수정: `findByUserId` → `findByUserEntityId` (엔티티 필드명 불일치)
    - NaverProvider `@Value` 경로 변경: `spring.security.oauth2.client.registration.naver.*` → `naver.*` (OAuth2 자동 설정 충돌 방지)
    - UserEntity.createWithEmail() providerId를 이메일 기반 UUID v3로 생성 (NOT NULL 제약조건 위반 수정)
    - build.gradle `jar { enabled = false }` 추가 (plain JAR 생성 방지)
17. 파일 로깅 추가 (2026-06-08)
    - logback-spring.xml: app.log (전체) + error.log (ERROR만) + 일별 롤링
    - Docker 볼륨 마운트로 호스트에서 로그 파일 접근 가능
18. 멀티 프로필 구성 (2026-06-08)
    - application.yml을 공통 설정만 남기고 5개 프로필로 분리
    - local (IntelliJ 직접 실행), local-dev (로컬 Docker), dev/stg/prd (AWS)
    - docker-compose.yml을 SPRING_PROFILES_ACTIVE=local-dev 한 줄로 간소화
    - stg/prd에서 Swagger UI 비활성화

19. SecurityConfig에 CORS 설정 추가 (2026-06-11)
    - Flutter 웹 앱에서 백엔드 API 호출 시 CORS preflight 404 해결
    - `localhost:*` 패턴 허용, credentials 포함
20. Flutter 앱 전체 구현 (2026-06-11)
    - 프로젝트 위치: ../brd_app
    - 기술 스택: Riverpod(상태관리), Dio(HTTP), GoRouter(라우팅), flutter_secure_storage(JWT)
    - 아키텍처: Clean Architecture (data/domain/presentation 3계층)
    - Auth: 이메일 로그인/회원가입, JWT 토큰 저장/갱신, 인증 가드
    - Bike: 목록/상세/등록/수정/삭제, 대표 바이크 설정
    - Maintenance: 정비 기록 CRUD, 정비 스케줄 CRUD, overdue 알림
    - Home: 대시보드 (대표 바이크 요약, 정비 필요 알림, 빠른 메뉴)
    - 화면 12개, Dart 파일 약 40개
    - 웹(Chrome)에서 개발 중 (AVD 미해결)
    - api_config.dart: kIsWeb으로 웹/에뮬레이터 baseUrl 자동 분기

21. 연비/주유(Fueling) 도메인 구현 (2026-06-16)
    - FuelingEntity: BigDecimal(8,2) 정밀 주유량, FuelType enum (REGULAR/PREMIUM/DIESEL)
    - 만탱크법(Full-tank method) 연비 계산: 이전 만탱크 기록 ~ 현재 기록 사이 거리/누적주유량
    - CRUD + 통계 엔드포인트 (6개): GET/POST/PUT/DELETE /fuelings, GET /fuelings/stats
    - FuelingRepository: 이전 만탱크 조회, 구간 주유량 합계 @Query
    - 단위 테스트 16개 (CRUD, 접근 권한, 연비 계산, 통계)
    - ErrorCode에 FUELING_ACCESS_DENIED 추가
    - schema.sql에 fuelings 테이블 + 인덱스 2개 추가
22. Flutter 앱 주유 기능 + 전체 UI 현대화 (2026-06-16)
    - 주유 데이터 레이어: model/repository/provider (FamilyAsyncNotifier)
    - 주유 목록: SliverAppBar 통계 헤더 + 바이크 선택 드롭다운 + 카드 리스트
    - 주유 폼: 주유량/단가 자동 계산, 연료 종류, 만탱크 스위치
    - 디자인 시스템: deep blue #1B2838 + orange accent #FF6B35
    - 전체 화면 그래디언트 헤더 + 카드 기반 레이아웃으로 통일
    - StatefulShellRoute 4탭 하단 네비게이션 (홈/바이크/주유/설정)
    - 로그인/회원가입: 그래디언트 + 흰색 카드 오버레이 디자인
    - 설정 화면: 프로필 카드 + 앱 정보 + 로그아웃

### 다음 단계

- Flutter 앱 백엔드 연동 테스트 (로그인 → 바이크 → 정비 → 주유 흐름)
- 소셜 로그인 (카카오/네이버) — 네이티브 SDK 연동 (AVD 해결 후)
- 라이딩 코스(Course) 도메인 (GPX 기록/업로드)

---

## Claude에게 요청하는 작업 방식

- 코드 생성 시 전체 파일 단위로 작성해줘 (일부만 발췌 말고)
- 새 기능 추가 전에 기존 코드 구조 먼저 파악하고 일관성 유지해줘
- Spring Boot 관련 베스트 프랙티스 적극 반영해줘
- Vue / Flutter는 내가 배우면서 하는 거라 코드에 설명 주석 달아줘
- 보안 이슈 발견하면 먼저 알려줘
