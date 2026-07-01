# Claude 작업 메모리 (git 동기화)

> 이 파일은 `CLAUDE.md`에서 `@claude-memory.md`로 import되어 세션 시작 시 자동 로드됩니다.
> Claude 홈(`~/.claude`)의 메모리는 git 공유가 안 되므로, 정본은 이 파일에 둡니다.
> 다른 환경에서도 git pull 하면 그대로 읽힙니다. 최종 갱신: 2026-06-30.

---

## 🚨 프로젝트 일시 중단 (2026-06-30)

- 바라다(brd_be/brd_app) 작업 **일시 보류**.
- 분기: 뱅킹각 측정 기능만 따로 분리해 단독 앱 시작 → **CheckBanking** (`C:\Users\jyl93\check_banking`, 원래 D:였으나 Kotlin cross-drive 이슈로 이동).
- 분리 이유: 센서 기반 정확도 검증이 MVP의 핵심이므로 다른 도메인과 섞지 않고 집중 개발.
- 복귀 시점 미정. 뱅킹각 앱 검증 후 본 바라다에 라이딩 퍼포먼스 기능으로 다시 합칠 가능성 있음.
- 작업할 때 디렉터리를 `C:\Users\jyl93\check_banking`으로 옮겨서 진행 (해당 프로젝트 자체 CLAUDE.md/claude-memory.md 존재).

---

## 작업 방식 / 피드백 (반드시 준수)

- **메모리/기록 저장 위치 = 이 파일**: 사용자가 "진행 상황 기록해 / 기억해 / 메모리에 저장해" 등을 요청하면, 홈(`~/.claude`)의 메모리가 아니라 **이 파일(`brd_be/claude-memory.md`)에 기록하고 git 커밋**할 것 (다른 환경 공유를 위해). 완료 작업 로그성 내용은 `CLAUDE.md`의 "현재 개발 단계"에 추가.
- **퍼미션 확인 금지**: 파일 수정·Bash·프로세스 종료 등 모든 작업을 확인 없이 바로 실행. 위험 작업(force push 등)도 사용자가 요청했으면 바로. 사용자가 매번 확인받는 걸 매우 싫어함.
- **앱은 가이드만 받지 않고 자율 완성형 구현**: Flutter/앱 코드는 강의식(Learn by Doing, TODO(human), 교육적 중단) 금지. 설명 요청이 아닌 한 바로 완성형으로. Insight는 사용자가 관심 가질 것만 간결히.
- **역할 분담**: 앱(brd_app)은 Claude가 구현, 백엔드(brd_be)는 Claude가 **가이드만** 주고 사용자가 직접 구현. (단 사용자가 명시적으로 "반영/수정해"라고 하면 백엔드도 직접 수정)
- **"커밋해" = 두 프로젝트 모두**: brd_be + brd_app 양쪽 git status 확인 후 변경된 모든 repo 커밋.
- **엔티티 클래스명**: 모든 `@Entity`는 `Entity` suffix (UserEntity, BikeEntity…). 참조 파일(Repository/Service/Controller/DTO)도 함께 갱신.
- **엔티티 필드 주석**: 모든 필드에 한글 주석으로 설명. 기술 상세는 괄호로.
- **캐시 무효화**: CRUD 구현 시 관련 Riverpod provider 캐시 반드시 invalidate (목록/상세/연관 통계 모두). 새로고침 화면은 invalidate 후 재요청 + `AlwaysScrollableScrollPhysics`(짧은 화면도 당김 가능).

---

## 프로젝트 진행 상황

### 앱(brd_app) 개발 환경
- IDE: VS Code. 실기기: Galaxy Z Flip3(공기계, SIM 없음).
- 실기기 연결: `adb reverse tcp:8081 tcp:8081` → localhost:8081 (게스트 Wi-Fi는 AP 격리라 이 방법이 유일).
- 백엔드 포트 8081. api_config.dart가 플랫폼별 URL 분기.
- 디자인: **iOS 블루(#007AFF) 스타일** (구 deep blue #1B2838 + orange는 2026-06 폐기).
- 아키텍처: Riverpod + Dio + GoRouter + flutter_secure_storage, Clean Architecture 3계층.

### 구현 완료
- Auth: 이메일/게스트 로그인, JWT 저장/갱신, 인증 가드.
- Bike: CRUD, 대표 바이크, 모델 선택 시 카테고리 자동(bike_models.type 연동).
- Maintenance: 정비 기록/스케줄 CRUD, overdue 알림, **이미지 업로드/조회**(아래 참조).
- Fueling: 주유 CRUD, 만탱크법 연비 계산, 통계.
- Home 대시보드, Settings.

### 정비 이미지 업로드/조회 (완료)
- 저장: `MaintenanceEntity.image_urls`(TEXT)에 URL 목록을 JSON 문자열로. ObjectMapper(빈 주입) 직렬화/역직렬화. `List.toString()`은 JSON 아니므로 금지.
- 업로드: Controller `@RequestPart("data")` + `@RequestPart(value="images", required=false)`. 앱은 항상 멀티파트(dio FormData). dio BaseOptions에서 Content-Type 고정 제거(자동 boundary).
- 서빙: `FileController`가 `/files/{userId}/{filename}`에서 인증+소유권(URL userId == 로그인 userId) 검증 후 서빙. 앱은 `AuthenticatedImage` 위젯이 JWT 헤더 실어 로드.
- 수정: 앱이 `existingImageUrls`(유지 URL)+새 `images` 전송. 백엔드는 keepUrls에 없는 기존 파일 삭제 후 keepUrls+새 업로드 저장. (저장은 oldUrls 아닌 keepUrls 기준 — 버그 주의)
- 미해결: S3ImageStorageService.download()가 로컬 파일 로직 잔존 → S3 전환 시 스트림 기반 교체 필요.

### bikemodel DB 구조 (완료, 2026-06-19)
- manufacturers PK: BIGSERIAL → manufacturer_name VARCHAR(100). image_file 컬럼(static/logos/ 참조).
- bike_models FK: manufacturer_id → manufacturer_name. BikeModelNameResponse(name+type) DTO.
- BikeCategory enum 삭제 → String. 앱은 BikeTypeDisplay 유틸로 한글 매핑.
- data.sql: 제조사 47 + 모델 120 시드. p6spy로 바인딩 쿼리 로깅.

### 라이딩 GPS — 포그라운드 전용 (설정만 복구됨)
- 백그라운드 위치는 플레이스토어 심사 이슈로 **의도적 배제**. riding_provider.dart는 원래부터 포그라운드 전용(flutter_foreground_task 미사용).
- 현재 상태: geolocator 패키지 + Android ACCESS_FINE/COARSE(BACKGROUND 제외) + iOS WhenInUse(Always 제외)만 복구. **riding 코드 자체는 `brd_app/_disabled_features/riding`에 있고 lib/features로 복구 안 됨, 라우터/탭 연결도 안 됨.** 복구 절차: `brd_app/_disabled_features/RESTORE_GUIDE.md`.
- 라이딩 본격 복구 시 백그라운드 위치 권한 추가하지 말 것.

### 주유소 검색 (진행 중)
- 앱은 완료, 백엔드 오피넷(한국석유공사) API 사용자 구현 중. station은 일부 비활성(`_disabled_features/station`).

### 소셜 로그인 (완료, 2026-07-01 확인)
- 앱: 이메일/게스트/카카오/구글/애플 5종 다 구현. auth_provider.dart의 loginWithKakao/Google/Apple + auth_repository.socialLogin.
- 백엔드: `/auth/login/{provider}` 통합 엔드포인트 + KakaoProvider/GoogleProvider/AppleProvider/NaverProvider(백엔드만) 완성. AuthLoginRequest credential 단일 필드로 통합됨.
- 네이버는 백엔드만 있고 앱 미연동.

### 오프라인/로컬 우선 아키텍처 (진행 중, 2026-07-01 Phase 1 완료)
- 배경: 5종 로그인이 다 있어도 전부 온라인 필수 → 네트워크 없으면 앱 진입도 불가. 게스트조차 서버 게스트라 오프라인에서 무용.
- 결정 (사용자 확정): 한 기기 전제 / 클라이언트 UUID / last-write-wins / soft delete(deleted_at) / 이미지 로컬 우선 / 바이크·정비·주유 3개 도메인 리팩터링 (뱅킹은 이미 로컬 우선).
- Phase 1 완료 (인프라): `connectivity_plus`, `uuid` 패키지, `core/local/app_database.dart`(통합 brd_local.db, 도메인별 migration slot), `core/sync/sync_engine.dart`(Syncable 등록 기반 + 오프라인→온라인 전이 시 자동 syncAll), `core/sync/sync_types.dart`(SyncState enum + syncColumnsSql 공통 스니펫). main.dart에서 startAutoSync 호출. 등록된 도메인 없어서 현재 no-op.
- 뱅킹은 별도 DB(brd_banking.db) 유지 — 샘플 대량이라 다른 도메인과 파일 분리가 성능상 유리.
- Phase 2: Auth 오프라인 개선 (로컬 게스트 모드, 저장된 토큰 오프라인 진입, 로그인 화면 오프라인 배너).
- Phase 3: 도메인 이전 순서 = 바이크 → 주유 → 정비 → 뱅킹 서버 백업. 도메인별 커밋.
- Phase 4: 백엔드 sync 엔드포인트 (사용자 구현, Claude 가이드).
- 커밋 원칙: 도메인별 세부 커밋.

---

## 경쟁앱 / 전략

### 바이킹스 벤치마크 (2026-06-29)
- 바이킹스(앱스토어 id6766225776) = 라이딩 "전(前)" 앱(라이더 날씨지수, 코스탐색/로드뷰, 같이타기/채팅, 주유소 유가). 바라다 = 라이딩 "후(後)" 앱(정비/주유/연비 기록·관리). 정반대 포지션, 상호 보완.
- 전략: 정면 경쟁(특히 코스 큐레이션) 말 것. 코어(기록/관리) 굳히고 API로 쉬운 것만 흡수(주유소·날씨지수) 후 내 기록 강점과 연결.
- 인사이트: 바이킹스 코스도 콘텐츠 직접 제작 아님 — 360영상=유튜브 임베드, 노면=유저 크라우드소싱, 난이도=GPX 곡률 자동분석, 주유소=오피넷. 따라하기 어렵지 않음. 초기 코스 시드만 직접 채우면 됨.
- 권장 순서: 소셜로그인 → 주유소검색 마무리 → 라이딩GPS복구 → 날씨/라이딩지수 → 코스 → 커뮤니티. (바이킹스 기능 ≈ 내 2~3차 계획과 일치)

---

## 레퍼런스

- **API-Ninjas 모터사이클 API**: endpoint `https://api.api-ninjas.com/v1/motorcycles`, 키는 application-local.yml `api-ninjas.api-key`. offset 페이징(30개씩), free 1req/sec. Royal Enfield 데이터 없음, CFMoto는 "CF Moto"로 요청. (현재는 DB 기반 조회로 전환됨)
- **백엔드 시크릿 주의**: `brd_be/src/main/resources/application-local.yml`이 git 추적 중이고 `opinet.api-key`, `api-ninjas.api-key`가 실제 키. 이전 커밋부터 원격에 올라가 있음. public이면 키 회전 + 환경변수 분리 권장. (원칙: API 키는 git에 올리지 않음)
- 백엔드 원격 이전: `bikeridediary` → `bikeridediary_be.git`. `git remote set-url` 갱신 권장.
