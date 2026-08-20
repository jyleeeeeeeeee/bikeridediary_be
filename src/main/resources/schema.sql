-- ============================================================
-- BikeRideDiary (바라다) PostgreSQL 스키마
--
-- 구성 원칙: 각 테이블별로 SEQUENCE → CREATE TABLE → INDEX → ALTER 순으로 모음.
-- Spring ScriptUtils는 세미콜론으로 문장을 스플릿하므로 DO $$ ... $$ 블록/PL/pgSQL 사용 금지.
-- ALTER는 IF NOT EXISTS / IF EXISTS로 idempotent 처리.
-- 초기 데이터(제조사/바이크 모델 등)는 data.sql에서 Hibernate 초기화 이후 실행.
--
-- RefreshToken은 Redis에 저장되므로 여기 없음.
-- ============================================================

-- UUID 생성 함수 (PostgreSQL 13+ 기본 내장 — gen_random_uuid)
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- 1. users (사용자)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS users_no_seq;

CREATE TABLE IF NOT EXISTS users (
    no                BIGINT       UNIQUE DEFAULT nextval('users_no_seq'),  -- 조회용 친숙 번호
    id                UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    provider          VARCHAR(20)  NOT NULL,
    provider_id       VARCHAR(255) NOT NULL,
    email             VARCHAR(255),
    password          VARCHAR(255),
    nickname          VARCHAR(50)  NOT NULL,
    profile_image_url VARCHAR(255),
    fcm_token         VARCHAR(255),
    role              VARCHAR(20)  NOT NULL DEFAULT 'USER',
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP,

    CONSTRAINT uq_users_provider UNIQUE (provider, provider_id),
    CONSTRAINT chk_users_role    CHECK (role IN ('USER', 'ADMIN'))
);

-- findByEmailAndDeletedAtIsNull, existsByEmailAndDeletedAtIsNull
CREATE INDEX IF NOT EXISTS idx_users_email_deleted_at ON users (email, deleted_at);

-- 레거시: 기존 DB에 role 없던 시절 대응
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role;
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('USER', 'ADMIN'));

-- 레거시: no 컬럼 보장 (기존 DB용)
ALTER TABLE users ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE users ALTER COLUMN no SET DEFAULT nextval('users_no_seq');


-- ============================================================
-- 2. bikes (바이크)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS bikes_no_seq;

CREATE TABLE IF NOT EXISTS bikes (
    no                       BIGINT       UNIQUE DEFAULT nextval('bikes_no_seq'),  -- 조회용 친숙 번호
    id                       UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id                  UUID         NOT NULL REFERENCES users(id),
    manufacturer_name        VARCHAR(100) NOT NULL,
    model_name               VARCHAR(100) NOT NULL,
    year                     INTEGER      NOT NULL,
    category                 VARCHAR(50),
    total_mileage_km         BIGINT       NOT NULL DEFAULT 0,
    is_representative        BOOLEAN      NOT NULL DEFAULT FALSE,
    purchased_at             DATE,
    photo_url                VARCHAR(255),
    memo                     VARCHAR(500),
    latest_fuel_efficiency   NUMERIC(6,2),
    average_fuel_efficiency  NUMERIC(6,2),
    is_exist_model           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at               TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP,
    deleted_at               TIMESTAMP
);

-- findByUserEntityIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc
CREATE INDEX IF NOT EXISTS idx_bikes_user_id_deleted_at
    ON bikes (user_id, deleted_at);
-- findByUserEntityIdAndIsRepresentativeTrueAndDeletedAtIsNull
CREATE INDEX IF NOT EXISTS idx_bikes_user_id_representative_deleted_at
    ON bikes (user_id, is_representative, deleted_at);

-- 레거시: INTEGER → BIGINT (Entity Long 필드 반영)
ALTER TABLE bikes ALTER COLUMN total_mileage_km TYPE BIGINT;

-- 레거시: no 컬럼 보장
ALTER TABLE bikes ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE bikes ALTER COLUMN no SET DEFAULT nextval('bikes_no_seq');


-- ============================================================
-- 3. maintenances (정비 이력)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS maintenances_no_seq;

CREATE TABLE IF NOT EXISTS maintenances (
    no                     BIGINT       UNIQUE DEFAULT nextval('maintenances_no_seq'),  -- 조회용 친숙 번호
    id                     UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id                UUID         NOT NULL REFERENCES bikes(id),
    maintenance_type       VARCHAR(20)  NOT NULL,
    maintenance_date       DATE         NOT NULL,
    mileage_at_maintenance BIGINT       NOT NULL,
    cost                   BIGINT,
    description            VARCHAR(500),
    next_due_km            BIGINT,
    next_due_date          DATE,
    created_at             TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP,
    deleted_at             TIMESTAMP
);

-- findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc
CREATE INDEX IF NOT EXISTS idx_maintenances_bike_id_deleted_at
    ON maintenances (bike_id, deleted_at);
-- findByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMaintenanceDateDesc
CREATE INDEX IF NOT EXISTS idx_maintenances_bike_id_type_deleted_at
    ON maintenances (bike_id, maintenance_type, deleted_at);

-- 레거시: INTEGER → BIGINT
ALTER TABLE maintenances ALTER COLUMN mileage_at_maintenance TYPE BIGINT;
ALTER TABLE maintenances ALTER COLUMN cost                   TYPE BIGINT;
ALTER TABLE maintenances ALTER COLUMN next_due_km            TYPE BIGINT;

-- 레거시: no 컬럼 보장
ALTER TABLE maintenances ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE maintenances ALTER COLUMN no SET DEFAULT nextval('maintenances_no_seq');


-- ============================================================
-- 4. maintenance_schedules (정비 주기)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS maintenance_schedules_no_seq;

CREATE TABLE IF NOT EXISTS maintenance_schedules (
    no                BIGINT       UNIQUE DEFAULT nextval('maintenance_schedules_no_seq'),  -- 조회용 친숙 번호
    id                UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id           UUID         NOT NULL REFERENCES bikes(id),
    maintenance_type  VARCHAR(20)  NOT NULL,
    interval_km       BIGINT,
    interval_months   INTEGER,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

-- findByBikeEntityIdAndDeletedAtIsNull
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_bike_id_deleted_at
    ON maintenance_schedules (bike_id, deleted_at);
-- existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull (중복 체크)
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_bike_id_type_deleted_at
    ON maintenance_schedules (bike_id, maintenance_type, deleted_at);

-- 레거시: INTEGER → BIGINT
ALTER TABLE maintenance_schedules ALTER COLUMN interval_km TYPE BIGINT;

-- 레거시: no 컬럼 보장
ALTER TABLE maintenance_schedules ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE maintenance_schedules ALTER COLUMN no SET DEFAULT nextval('maintenance_schedules_no_seq');


-- ============================================================
-- 5. fuelings (주유 기록)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS fuelings_no_seq;

CREATE TABLE IF NOT EXISTS fuelings (
    no                  BIGINT         UNIQUE DEFAULT nextval('fuelings_no_seq'),  -- 조회용 친숙 번호
    id                  UUID           DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id             UUID           NOT NULL REFERENCES bikes(id),
    fueling_date        DATE           NOT NULL,
    mileage_at_fueling  BIGINT         NOT NULL,
    fuel_amount         NUMERIC(8,2)   NOT NULL,
    price_per_liter     BIGINT,
    total_cost          BIGINT,
    fuel_type           VARCHAR(10)    NOT NULL,
    fuel_efficiency     NUMERIC(6,2),
    memo                VARCHAR(500),
    station_name        VARCHAR(100),
    created_at          TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP,
    deleted_at          TIMESTAMP
);

-- findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDesc
CREATE INDEX IF NOT EXISTS idx_fuelings_bike_id_deleted_at
    ON fuelings (bike_id, deleted_at);
-- 연비 계산용 (이전 주유 기록 조회)
CREATE INDEX IF NOT EXISTS idx_fuelings_bike_id_mileage
    ON fuelings (bike_id, mileage_at_fueling, deleted_at);

-- 레거시: INTEGER → BIGINT
ALTER TABLE fuelings ALTER COLUMN mileage_at_fueling TYPE BIGINT;
ALTER TABLE fuelings ALTER COLUMN price_per_liter    TYPE BIGINT;
ALTER TABLE fuelings ALTER COLUMN total_cost         TYPE BIGINT;

-- 레거시: no 컬럼 보장
ALTER TABLE fuelings ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE fuelings ALTER COLUMN no SET DEFAULT nextval('fuelings_no_seq');


-- ============================================================
-- 6. manufacturers (제조사 마스터)
-- 초기 데이터는 data.sql에서 관리
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS manufacturers_no_seq;

CREATE TABLE IF NOT EXISTS manufacturers (
    no                BIGINT       UNIQUE DEFAULT nextval('manufacturers_no_seq'),  -- 조회용 친숙 번호
    manufacturer_name VARCHAR(100) PRIMARY KEY,
    display_name_ko   VARCHAR(100) NOT NULL,
    country           VARCHAR(50),
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order     INTEGER      NOT NULL DEFAULT 999,
    image_file        VARCHAR(200)
);

-- 레거시: ddl-auto:update가 컬럼을 삭제하지 않아 수동 정리
ALTER TABLE manufacturers DROP COLUMN IF EXISTS created_at;
ALTER TABLE manufacturers DROP COLUMN IF EXISTS updated_at;

-- 레거시: no 컬럼 보장
ALTER TABLE manufacturers ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE manufacturers ALTER COLUMN no SET DEFAULT nextval('manufacturers_no_seq');


-- ============================================================
-- 7. bike_models (바이크 모델 마스터)
-- 초기 데이터는 data.sql에서 관리
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS bike_models_no_seq;

CREATE TABLE IF NOT EXISTS bike_models (
    no                BIGINT       UNIQUE DEFAULT nextval('bike_models_no_seq'),  -- 조회용 친숙 번호 (PK id와 별개)
    id                BIGSERIAL    PRIMARY KEY,
    manufacturer_name VARCHAR(100) NOT NULL REFERENCES manufacturers(manufacturer_name),
    name              VARCHAR(150) NOT NULL,
    year              INTEGER,
    type              VARCHAR(50),
    displacement      VARCHAR(100),
    engine            VARCHAR(200),
    power             VARCHAR(150),
    torque            VARCHAR(150),
    total_weight      VARCHAR(100),
    seat_height       VARCHAR(100),
    fuel_capacity     VARCHAR(100),

    CONSTRAINT uq_bike_models_mfr_name_year UNIQUE (manufacturer_name, name, year)
);

-- 제조사별 모델 조회
CREATE INDEX IF NOT EXISTS idx_bike_models_manufacturer_name
    ON bike_models (manufacturer_name);

-- 레거시: ddl-auto:update가 컬럼을 삭제하지 않아 수동 정리
ALTER TABLE bike_models DROP COLUMN IF EXISTS created_at;

-- 레거시: no 컬럼 보장
ALTER TABLE bike_models ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE bike_models ALTER COLUMN no SET DEFAULT nextval('bike_models_no_seq');


-- ============================================================
-- 8. place_categories (장소 카테고리 마스터)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS place_categories_no_seq;

CREATE TABLE IF NOT EXISTS place_categories (
    no            BIGINT       UNIQUE DEFAULT nextval('place_categories_no_seq'),  -- 조회용 친숙 번호
    category_code VARCHAR(50)  PRIMARY KEY,
    category_name VARCHAR(50)  NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP
);

-- 레거시: no 컬럼 보장
ALTER TABLE place_categories ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE place_categories ALTER COLUMN no SET DEFAULT nextval('place_categories_no_seq');


-- ============================================================
-- 9. places (라이더 큐레이션 POI)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS places_no_seq;

CREATE TABLE IF NOT EXISTS places (
    no             BIGINT        UNIQUE DEFAULT nextval('places_no_seq'),  -- 조회용 친숙 번호
    id             UUID          DEFAULT gen_random_uuid() PRIMARY KEY,
    place_name     VARCHAR(100)  NOT NULL,
    user_id        UUID          REFERENCES users(id) ON DELETE SET NULL,
    star_point     REAL,
    wished_count   INTEGER       NOT NULL DEFAULT 0,
    category_code  VARCHAR(50)   NOT NULL REFERENCES place_categories(category_code) ON DELETE RESTRICT,
    latitude       NUMERIC(9,7)  NOT NULL,
    longitude      NUMERIC(10,7) NOT NULL,
    address        VARCHAR(200),
    road_address   VARCHAR(200),
    description    TEXT,
    photo_url      VARCHAR(500),
    phone          VARCHAR(30),
    kakao_place_id VARCHAR(50),
    naver_place_id VARCHAR(50),
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP,
    deleted_at     TIMESTAMP
);

-- findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc
CREATE INDEX IF NOT EXISTS idx_places_category_deleted_at
    ON places (category_code, deleted_at);
-- 좌표 반경 검색 (PostGIS 도입 전 NUMERIC 기반, partial: 삭제된 레코드 제외)
CREATE INDEX IF NOT EXISTS idx_places_lat_lng
    ON places (latitude, longitude)
    WHERE deleted_at IS NULL;

-- 레거시: no 컬럼 보장
ALTER TABLE places ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE places ALTER COLUMN no SET DEFAULT nextval('places_no_seq');
-- 참고: UNIQUE 제약은 ScriptUtils의 DO 블록 미지원으로 여기서 못 붙임.
-- 필요 시 수동 SQL: ALTER TABLE places ADD CONSTRAINT uq_places_no UNIQUE (no);


-- ============================================================
-- 10. place_wishes (장소 찜)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS place_wishes_no_seq;

CREATE TABLE IF NOT EXISTS place_wishes (
    no         BIGINT    UNIQUE DEFAULT nextval('place_wishes_no_seq'),  -- 조회용 친숙 번호
    place_id   UUID      NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (place_id, user_id)
);

-- MY탭에서 user 기준 조회 (PK가 (place_id, user_id)라 user 단독 인덱스 별도 필요)
CREATE INDEX IF NOT EXISTS idx_place_wishes_user_id
    ON place_wishes (user_id);

-- 레거시: no 컬럼 보장
ALTER TABLE place_wishes ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE place_wishes ALTER COLUMN no SET DEFAULT nextval('place_wishes_no_seq');


-- ============================================================
-- 11. courses (라이딩 코스)
-- deleted_at 없음: hard delete 정책 (waypoints/favorites CASCADE 자동 삭제)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS courses_no_seq;

CREATE TABLE IF NOT EXISTS courses (
    no               BIGINT        UNIQUE DEFAULT nextval('courses_no_seq'),  -- 조회용 친숙 번호
    id               UUID          DEFAULT gen_random_uuid() PRIMARY KEY,
    -- user_id nullable: 시드/큐레이션 코스는 작성자 없음
    -- ON DELETE SET NULL: 유저 탈퇴 시 코스 콘텐츠는 유지
    user_id          UUID          REFERENCES users(id) ON DELETE SET NULL,
    name             VARCHAR(100)  NOT NULL,
    distance_meters  INTEGER       NOT NULL,
    path             TEXT          NOT NULL,
    -- 경로 바운딩 박스 (JSON 문자열: [[minLng,minLat],[maxLng,maxLat]]). fitBounds용
    bbox             TEXT,
    is_public        BOOLEAN       NOT NULL DEFAULT TRUE,
    -- 원본 코스 참조 — 원본 hard delete 시 SET NULL로 참조만 끊고 파생 코스는 유지
    source_course_id UUID          REFERENCES courses(id) ON DELETE SET NULL,
    description      TEXT,
    -- 카운트 (view: 비소유자 조회, like: favorite, copy: 파생, navigate: 지도 딥링크)
    view_count       BIGINT        NOT NULL DEFAULT 0,
    like_count       BIGINT        NOT NULL DEFAULT 0,
    copy_count       BIGINT        NOT NULL DEFAULT 0,
    navigate_count   BIGINT        NOT NULL DEFAULT 0,
    created_at       TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP
);

-- 내 코스 조회 (user_id 기준)
CREATE INDEX IF NOT EXISTS idx_courses_user_id
    ON courses (user_id);
-- 탐색 목록 - 공개 코스만 (partial index)
CREATE INDEX IF NOT EXISTS idx_courses_public
    ON courses (is_public)
    WHERE is_public = TRUE;
-- 최신순 정렬 지원 (partial: 공개 코스만)
CREATE INDEX IF NOT EXISTS idx_courses_updated_at
    ON courses (updated_at DESC)
    WHERE is_public = TRUE;

-- 레거시: description/카운트/bbox 컬럼 (기존 DB용)
ALTER TABLE courses ADD COLUMN IF NOT EXISTS description    TEXT;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS view_count     BIGINT NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS like_count     BIGINT NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS copy_count     BIGINT NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS navigate_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE courses ADD COLUMN IF NOT EXISTS bbox           TEXT;

-- 레거시: no 컬럼 보장
ALTER TABLE courses ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE courses ALTER COLUMN no SET DEFAULT nextval('courses_no_seq');


-- ============================================================
-- 12. course_waypoints (코스 경유지)
-- seq (SMALLINT): waypoint 순서 인덱스 (0-based)
-- no  (BIGINT):   조회용 친숙 번호 (신규 추가)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS course_waypoints_no_seq;

CREATE TABLE IF NOT EXISTS course_waypoints (
    no        BIGINT        UNIQUE DEFAULT nextval('course_waypoints_no_seq'),  -- 조회용 친숙 번호
    id        UUID          DEFAULT gen_random_uuid() PRIMARY KEY,
    course_id UUID          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    seq       SMALLINT      NOT NULL,
    role      VARCHAR(10)   NOT NULL,
    -- 등록된 place 참조 (옵셔널) — 임의 지점(지도 롱프레스, GPX 임포트)은 NULL
    -- ON DELETE SET NULL: place 삭제되어도 좌표 스냅샷이 남아 코스는 유효
    place_id  UUID          REFERENCES places(id) ON DELETE SET NULL,
    -- 좌표/이름은 스냅샷 (place 수정/삭제에도 코스는 그때 그대로 유지)
    name      VARCHAR(100),
    latitude  NUMERIC(9,7)  NOT NULL,
    longitude NUMERIC(10,7) NOT NULL,

    CONSTRAINT chk_waypoint_role      CHECK (role IN ('START', 'VIA', 'GOAL')),
    CONSTRAINT uq_waypoint_course_seq UNIQUE (course_id, seq)
);

-- place 역방향 조회 (특정 place를 지나는 코스 찾기 - 커뮤니티 확장 대비)
CREATE INDEX IF NOT EXISTS idx_course_waypoints_place_id
    ON course_waypoints (place_id)
    WHERE place_id IS NOT NULL;

-- 레거시: role END → GOAL 마이그레이션 (2026-08-04)
-- 예전 자동생성 이름(course_waypoints_role_check)도 방어적으로 drop
UPDATE course_waypoints SET role = 'GOAL' WHERE role = 'END';
ALTER TABLE course_waypoints DROP CONSTRAINT IF EXISTS course_waypoints_role_check;
ALTER TABLE course_waypoints DROP CONSTRAINT IF EXISTS chk_waypoint_role;
ALTER TABLE course_waypoints ADD CONSTRAINT chk_waypoint_role
    CHECK (role IN ('START', 'VIA', 'GOAL'));

-- 레거시: no 컬럼 보장
ALTER TABLE course_waypoints ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE course_waypoints ALTER COLUMN no SET DEFAULT nextval('course_waypoints_no_seq');


-- ============================================================
-- 13. course_favorites (코스 즐겨찾기)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS course_favorites_no_seq;

CREATE TABLE IF NOT EXISTS course_favorites (
    no         BIGINT    UNIQUE DEFAULT nextval('course_favorites_no_seq'),  -- 조회용 친숙 번호
    course_id  UUID      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (course_id, user_id)
);

-- user 기준 MY탭 조회 (PK가 course_id 시작이라 user 단독 인덱스 필요)
CREATE INDEX IF NOT EXISTS idx_course_favorites_user_id
    ON course_favorites (user_id);

-- 레거시: no 컬럼 보장
ALTER TABLE course_favorites ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE course_favorites ALTER COLUMN no SET DEFAULT nextval('course_favorites_no_seq');


-- ============================================================
-- 14. place_change_requests (장소 변경 요청 큐)
-- 유저의 신규 등록/좌표 수정/정보 수정을 어드민 승인 큐로 관리.
-- 승인되면 places 반영 후 status=APPROVED, 거절되면 status=REJECTED.
-- (승인 클릭 트랜잭션 내에서 즉시 places 반영)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS place_change_requests_no_seq;

CREATE TABLE IF NOT EXISTS place_change_requests (
    no               BIGINT       UNIQUE DEFAULT nextval('place_change_requests_no_seq'),  -- 조회용 친숙 번호
    id               UUID         DEFAULT gen_random_uuid() PRIMARY KEY,

    -- 요청 종류: CREATE / UPDATE_COORDINATES / UPDATE_INFO
    type             VARCHAR(30)  NOT NULL,

    -- 수정 대상 place (UPDATE_* 계열만 값 있음, CREATE는 NULL)
    -- ON DELETE CASCADE: place 삭제되면 관련 요청도 정리
    target_place_id  UUID         REFERENCES places(id) ON DELETE CASCADE,

    -- 요청자 (NOT NULL). 유저 탈퇴 시 요청 히스토리도 사라져도 무방
    requester_id     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- type별 payload (JSONB)
    --   CREATE:              { clientUuid, placeName, category, latitude, longitude,
    --                          address, roadAddress, description, phone, photoUrl }
    --   UPDATE_COORDINATES:  { latitude, longitude }
    --   UPDATE_INFO:         { placeName, category }
    payload          JSONB        NOT NULL,

    -- PENDING / APPROVED / REJECTED
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',

    -- 어드민이 남기는 승인/거절 사유
    review_note      TEXT,

    -- 검토한 어드민 (nullable, PENDING 상태에서는 NULL)
    reviewed_by      UUID         REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at      TIMESTAMP,

    created_at       TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT chk_pcr_type   CHECK (type IN ('CREATE', 'UPDATE_COORDINATES', 'UPDATE_INFO')),
    CONSTRAINT chk_pcr_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    -- CREATE 요청은 target 없어야 하고, UPDATE_* 는 target 필수
    CONSTRAINT chk_pcr_target CHECK (
        (type = 'CREATE' AND target_place_id IS NULL) OR
        (type IN ('UPDATE_COORDINATES', 'UPDATE_INFO') AND target_place_id IS NOT NULL)
    )
);

-- 어드민 요청 목록: status=PENDING 기준 최신순
CREATE INDEX IF NOT EXISTS idx_pcr_status_created
    ON place_change_requests (status, created_at DESC)
    WHERE status = 'PENDING';

-- 요청자 본인 목록 조회
CREATE INDEX IF NOT EXISTS idx_pcr_requester
    ON place_change_requests (requester_id, created_at DESC);

-- 특정 place에 대한 PENDING 존재 여부 조회 (중복 방지)
CREATE INDEX IF NOT EXISTS idx_pcr_target_pending
    ON place_change_requests (target_place_id)
    WHERE status = 'PENDING' AND target_place_id IS NOT NULL;

-- 중복 방지 UNIQUE (부분): 같은 target_place_id + PENDING 조합 유일
-- UPDATE_COORDINATES와 UPDATE_INFO를 통틀어 target 하나에 대해 PENDING 1개만 허용
CREATE UNIQUE INDEX IF NOT EXISTS uq_pcr_target_pending
    ON place_change_requests (target_place_id)
    WHERE status = 'PENDING' AND target_place_id IS NOT NULL;

-- 레거시: no 컬럼 보장
ALTER TABLE place_change_requests ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE place_change_requests ALTER COLUMN no SET DEFAULT nextval('place_change_requests_no_seq');


-- ============================================================
-- 15. api_call_logs (외부 API 호출 로그)
-- 목적: 사용량 모니터링 / 이상 탐지 / 유저별 차단 근거
-- 보관 정책: 90일 후 스케줄러가 DELETE
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS api_call_logs_no_seq;

CREATE TABLE IF NOT EXISTS api_call_logs (
    no               BIGINT        UNIQUE DEFAULT nextval('api_call_logs_no_seq'),
    id               UUID          DEFAULT gen_random_uuid() PRIMARY KEY,
    -- 호출 유저 (인증 없이 호출된 경우 null — 게스트/시스템 배치)
    -- ON DELETE SET NULL: 유저 탈퇴 시 로그 자체는 유지 (사용량 집계 무결성)
    user_id          UUID          REFERENCES users(id) ON DELETE SET NULL,
    -- API 식별자 (NAVER_DIRECTIONS, NAVER_GEOCODING, NAVER_REVERSE_GEOCODING,
    --                NAVER_SEARCH, KAKAO_LOCAL, OPINET, OPENWEATHER)
    api_name         VARCHAR(50)   NOT NULL,
    -- 실제 호출 URL path (쿼리스트링 제외)
    endpoint         VARCHAR(200)  NOT NULL,
    http_method      VARCHAR(10)   NOT NULL,
    -- 응답 HTTP 상태 코드 (네트워크 예외 시 null)
    status_code      INTEGER,
    -- 호출~응답 소요 시간 (밀리초)
    response_time_ms INTEGER       NOT NULL,
    -- 마스킹된 요청 파라미터 (apiKey/clientSecret/clientId 자동 제거 후 저장)
    request_params   JSONB,
    -- 실패 시 예외 메시지 (성공이면 null)
    error_message    TEXT,
    called_at        TIMESTAMP     NOT NULL DEFAULT now()
);

-- API별 최신 호출 조회 (관리자 API ?apiName=X + 최신순 정렬)
CREATE INDEX IF NOT EXISTS idx_api_logs_api_name_called_at
    ON api_call_logs (api_name, called_at DESC);
-- 유저별 사용량 조회 (?userId=X 필터). partial — 익명(null) 제외해 인덱스 크기 절약
CREATE INDEX IF NOT EXISTS idx_api_logs_user_id_called_at
    ON api_call_logs (user_id, called_at DESC)
    WHERE user_id IS NOT NULL;
-- 기간 조회 + 스케줄러 DELETE WHERE called_at < ?
CREATE INDEX IF NOT EXISTS idx_api_logs_called_at
    ON api_call_logs (called_at);

-- 레거시: no 컬럼 보장
ALTER TABLE api_call_logs ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE api_call_logs ALTER COLUMN no SET DEFAULT nextval('api_call_logs_no_seq');


-- ============================================================
-- 16. user_reports (유저 제보 — 버그/장소 삭제/기타)
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS user_reports_no_seq;

CREATE TABLE IF NOT EXISTS user_reports (
    no               BIGINT       UNIQUE DEFAULT nextval('user_reports_no_seq'),  -- 조회용 친숙 번호
    id               UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    title            VARCHAR(100) NOT NULL,
    -- 요청 종류: PLACE_DELETE / BUG_REPORT / ETC (enum 매핑)
    report_type      VARCHAR(30)  NOT NULL,
    -- 요청 내용 (PLACE_DELETE 시 이유 문자열, 대상 place는 target_place_id 참조)
    content          TEXT         NOT NULL,
    -- 처리 상태: REPORTED / PROCEEDING / DONE / REJECT
    status           VARCHAR(20)  NOT NULL DEFAULT 'REPORTED',
    -- 관리자 처리 응답 (반려 사유 or 처리 결과)
    reply            TEXT,
    -- 대상 place (report_type=PLACE_DELETE 등에서 사용, 그 외 NULL)
    -- ON DELETE SET NULL: place 삭제돼도 제보 히스토리는 유지
    target_place_id  UUID         REFERENCES places(id) ON DELETE SET NULL,
    -- 제보자. 유저 탈퇴 시 제보 히스토리는 사라져도 무방
    user_id          UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- 처리 완료 시각 (status DONE/REJECT 시 세팅)
    ended_at         TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    -- BaseEntity 상속이라 필요. 실제 소프트 삭제 UX는 없지만 스키마 검증 통과용
    deleted_at       TIMESTAMP,

    -- 검토한 어드민 (nullable, PENDING 상태에서는 NULL)
    reviewed_by      UUID         REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at      TIMESTAMP,


    CONSTRAINT chk_user_reports_type   CHECK (report_type IN ('PLACE_DELETE', 'BUG_REPORT', 'ETC')),
    CONSTRAINT chk_user_reports_status CHECK (status IN ('REPORTED', 'PROCEEDING', 'DONE', 'REJECT'))
);

-- 유저별 제보 내역 조회용
CREATE INDEX IF NOT EXISTS idx_user_reports_user_id
    ON user_reports (user_id);
-- 관리자 큐 조회용 (status + 최신순)
CREATE INDEX IF NOT EXISTS idx_user_reports_status_created_at
    ON user_reports (status, created_at DESC);

-- 레거시: 기존 pgAdmin DDL이 users_no_seq 잘못 참조하던 것 정정
ALTER TABLE user_reports ADD COLUMN IF NOT EXISTS no BIGINT;
ALTER TABLE user_reports ALTER COLUMN no SET DEFAULT nextval('user_reports_no_seq');

-- 레거시: target_place_id 신규 컬럼 (기존 DDL엔 없었음)
ALTER TABLE user_reports ADD COLUMN IF NOT EXISTS target_place_id UUID
    REFERENCES places(id) ON DELETE SET NULL;

-- 레거시: BaseEntity 상속 대응 (기존 DDL엔 없었음)
ALTER TABLE user_reports ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- 레거시: 어드민 감사 필드 (기존 DDL엔 없었음)
ALTER TABLE user_reports ADD COLUMN IF NOT EXISTS reviewed_by UUID
    REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE user_reports ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;

-- 레거시: CHECK 제약 (기존 데이터에 잘못된 값 있으면 실패 — 필요 시 먼저 UPDATE로 정규화)
ALTER TABLE user_reports DROP CONSTRAINT IF EXISTS chk_user_reports_type;
ALTER TABLE user_reports ADD CONSTRAINT chk_user_reports_type
    CHECK (report_type IN ('PLACE_DELETE', 'BUG_REPORT', 'ETC'));

ALTER TABLE user_reports DROP CONSTRAINT IF EXISTS chk_user_reports_status;
ALTER TABLE user_reports ADD CONSTRAINT chk_user_reports_status
    CHECK (status IN ('REPORTED', 'PROCEEDING', 'DONE', 'REJECT'));
