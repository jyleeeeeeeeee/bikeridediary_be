-- ============================================================
-- BikeRideDiary (바라다) PostgreSQL 스키마
-- 대상 Entity: UserEntity, BikeEntity, MaintenanceEntity,
--              MaintenanceScheduleEntity, FuelingEntity,
--              ManufacturerEntity, BikeModelEntity,
--              PlaceCategoryEntity, PlaceEntity,
--              CourseEntity, CourseWaypointEntity, CourseFavoriteEntity
-- RefreshToken은 Redis에 저장되므로 PostgreSQL 스키마에 포함하지 않음
-- ============================================================

-- UUID 생성 함수 활성화 (PostgreSQL 13 이상은 기본 내장)
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. users (사용자)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id                UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    provider          VARCHAR(20)  NOT NULL,
    provider_id       VARCHAR(255) NOT NULL,
    email             VARCHAR(255),
    password          VARCHAR(255),
    nickname          VARCHAR(50)  NOT NULL,
    profile_image_url VARCHAR(255),
    fcm_token         VARCHAR(255),
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP,

    CONSTRAINT uq_users_provider UNIQUE (provider, provider_id)
);

-- ============================================================
-- 2. bikes (바이크)
-- ============================================================
CREATE TABLE IF NOT EXISTS bikes (
    id                UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users(id),
    manufacturer_name VARCHAR(100) NOT NULL,
    model_name        VARCHAR(100) NOT NULL,
    year              INTEGER      NOT NULL,
    category          VARCHAR(50),
    total_mileage_km  BIGINT       NOT NULL DEFAULT 0,
    is_representative BOOLEAN      NOT NULL DEFAULT FALSE,
    purchased_at      DATE,
    photo_url         VARCHAR(255),
    memo              VARCHAR(500),
    latest_fuel_efficiency  NUMERIC(6,2),
    average_fuel_efficiency NUMERIC(6,2),
    is_exist_model    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

-- ============================================================
-- 3. maintenances (정비 이력)
-- ============================================================
CREATE TABLE IF NOT EXISTS maintenances (
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

-- ============================================================
-- 4. maintenance_schedules (정비 주기)
-- ============================================================
CREATE TABLE IF NOT EXISTS maintenance_schedules (
    id                       UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id                  UUID         NOT NULL REFERENCES bikes(id),
    maintenance_type         VARCHAR(20)  NOT NULL,
    interval_km              BIGINT,
    interval_months          INTEGER,
    created_at               TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP,
    deleted_at               TIMESTAMP
);

-- ============================================================
-- 5. fuelings (주유 기록)
-- ============================================================
CREATE TABLE IF NOT EXISTS fuelings (
    id                UUID           DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id           UUID           NOT NULL REFERENCES bikes(id),
    fueling_date      DATE           NOT NULL,
    mileage_at_fueling BIGINT        NOT NULL,
    fuel_amount       NUMERIC(8,2)   NOT NULL,
    price_per_liter   BIGINT,
    total_cost        BIGINT,
    fuel_type         VARCHAR(10)    NOT NULL,
    fuel_efficiency   NUMERIC(6,2),
    memo              VARCHAR(500),
    station_name      VARCHAR(100),
    created_at        TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

-- ============================================================
-- 6. manufacturers (제조사 마스터)
-- ============================================================
CREATE TABLE IF NOT EXISTS manufacturers (
    manufacturer_name VARCHAR(100) PRIMARY KEY,
    display_name_ko   VARCHAR(100) NOT NULL,
    country           VARCHAR(50),
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order     INTEGER      NOT NULL DEFAULT 999,
    image_file        VARCHAR(200)
);

-- ============================================================
-- 7. bike_models (바이크 모델 마스터)
-- ============================================================
CREATE TABLE IF NOT EXISTS bike_models (
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

-- ============================================================
-- 8. place_categories (장소 카테고리 마스터)
-- ============================================================
CREATE TABLE IF NOT EXISTS place_categories (
    category_code VARCHAR(50) PRIMARY KEY,
    category_name VARCHAR(50)  NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP
    );

-- ============================================================
-- 9. places (라이더 큐레이션 POI)
-- ============================================================
CREATE TABLE IF NOT EXISTS places (
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

-- ============================================================
-- 10. place_wishes (장소 찜)
-- ============================================================
CREATE TABLE IF NOT EXISTS place_wishes (
    place_id   UUID      NOT NULL REFERENCES places(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (place_id, user_id)
    );

-- ============================================================
-- 11. courses (라이딩 코스)
-- ============================================================
CREATE TABLE IF NOT EXISTS courses (
    id               UUID          DEFAULT gen_random_uuid() PRIMARY KEY,
    -- user_id nullable: 시드/큐레이션 코스는 작성자 없음 (관리자가 seed로 넣은 코스)
    -- ON DELETE SET NULL: 유저 탈퇴 시 코스 콘텐츠는 유지
    user_id          UUID          REFERENCES users(id) ON DELETE SET NULL,
    name             VARCHAR(100)  NOT NULL,
    distance_meters  INTEGER       NOT NULL,
    path             TEXT          NOT NULL,
    is_public        BOOLEAN       NOT NULL DEFAULT TRUE,
    -- 원본 코스 참조 — 원본 hard delete 시 SET NULL로 참조만 끊고 파생 코스는 유지
    source_course_id UUID          REFERENCES courses(id) ON DELETE SET NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP
    -- deleted_at 없음: courses는 hard delete 정책 (waypoints/favorites CASCADE 자동 삭제)
    );

-- ============================================================
-- 12. course_waypoints (코스 경유지)
-- ============================================================
CREATE TABLE IF NOT EXISTS course_waypoints (
    id         UUID          DEFAULT gen_random_uuid() PRIMARY KEY,
    course_id  UUID          NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    seq        SMALLINT      NOT NULL,
    role       VARCHAR(10)   NOT NULL,
    -- 등록된 place 참조 (옵셔널) — 임의 지점(지도 롱프레스, GPX 임포트)은 NULL
    -- ON DELETE SET NULL: place 삭제되어도 좌표 스냅샷이 남아 코스는 유효
    place_id   UUID          REFERENCES places(id) ON DELETE SET NULL,
    -- 좌표/이름은 스냅샷으로 저장 (place 수정/삭제에도 코스는 그때 그대로 유지)
    name       VARCHAR(100),
    latitude   NUMERIC(9,7)  NOT NULL,
    longitude  NUMERIC(10,7) NOT NULL,

    CONSTRAINT chk_waypoint_role CHECK (role IN ('START', 'VIA', 'END')),
    CONSTRAINT uq_waypoint_course_seq UNIQUE (course_id, seq)
    );

-- ============================================================
-- 13. course_favorites (코스 즐겨찾기)
-- ============================================================
CREATE TABLE IF NOT EXISTS course_favorites (
    course_id  UUID      NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    user_id    UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (course_id, user_id)
    );
-- ============================================================
-- 14. 인덱스
-- ============================================================
-- places: findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc
CREATE INDEX IF NOT EXISTS idx_places_category_deleted_at
    ON places (category_code, deleted_at);

-- places: 좌표 반경 검색 (PostGIS 도입 전 NUMERIC 기반, 삭제된 레코드 제외)
CREATE INDEX IF NOT EXISTS idx_places_lat_lng
    ON places (latitude, longitude)
    WHERE deleted_at IS NULL;

-- place_wishes: MY탭에서 user 기준 조회 (PK가 (place_id, user_id)라 user 단독 인덱스 별도 필요)
CREATE INDEX IF NOT EXISTS idx_place_wishes_user_id
    ON place_wishes (user_id);

-- courses: 내 코스 조회 (user_id 기준)
CREATE INDEX IF NOT EXISTS idx_courses_user_id
    ON courses (user_id);

-- courses: 탐색 목록 - 공개 코스만 (partial index)
CREATE INDEX IF NOT EXISTS idx_courses_public
    ON courses (is_public)
    WHERE is_public = TRUE;

-- courses: 최신순 정렬 지원
CREATE INDEX IF NOT EXISTS idx_courses_updated_at
    ON courses (updated_at DESC)
    WHERE is_public = TRUE;

-- course_favorites: user 기준 MY탭 조회 (PK가 course_id 시작이라 user 단독 인덱스 필요)
CREATE INDEX IF NOT EXISTS idx_course_favorites_user_id
    ON course_favorites (user_id);

-- course_waypoints: place 역방향 조회 (특정 place를 지나는 코스 찾기 - 커뮤니티 확장 대비)
-- place_id IS NOT NULL인 row만 포함하는 partial index
CREATE INDEX IF NOT EXISTS idx_course_waypoints_place_id
    ON course_waypoints (place_id)
    WHERE place_id IS NOT NULL;

-- bikes: findByUserEntityIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc
CREATE INDEX IF NOT EXISTS idx_bikes_user_id_deleted_at ON bikes (user_id, deleted_at);
-- bikes: findByUserEntityIdAndIsRepresentativeTrueAndDeletedAtIsNull
CREATE INDEX IF NOT EXISTS idx_bikes_user_id_representative_deleted_at ON bikes (user_id, is_representative, deleted_at);

-- maintenances: findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc
CREATE INDEX IF NOT EXISTS idx_maintenances_bike_id_deleted_at ON maintenances (bike_id, deleted_at);
-- maintenances: findByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMaintenanceDateDesc
CREATE INDEX IF NOT EXISTS idx_maintenances_bike_id_type_deleted_at ON maintenances (bike_id, maintenance_type, deleted_at);

-- maintenance_schedules: findByBikeEntityIdAndDeletedAtIsNull
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_bike_id_deleted_at ON maintenance_schedules (bike_id, deleted_at);
-- maintenance_schedules: existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull (중복 체크)
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_bike_id_type_deleted_at ON maintenance_schedules (bike_id, maintenance_type, deleted_at);

-- fuelings: findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDesc
CREATE INDEX IF NOT EXISTS idx_fuelings_bike_id_deleted_at ON fuelings (bike_id, deleted_at);
-- fuelings: 연비 계산용 (이전 주유 기록 조회)
CREATE INDEX IF NOT EXISTS idx_fuelings_bike_id_mileage ON fuelings (bike_id, mileage_at_fueling, deleted_at);

-- users: findByEmailAndDeletedAtIsNull, existsByEmailAndDeletedAtIsNull
CREATE INDEX IF NOT EXISTS idx_users_email_deleted_at ON users (email, deleted_at);

-- bike_models: 제조사별 모델 조회
CREATE INDEX IF NOT EXISTS idx_bike_models_manufacturer_name ON bike_models (manufacturer_name);

-- ============================================================
-- 9. 레거시 컬럼 정리 (ddl-auto:update는 컬럼을 삭제하지 않음)
-- ============================================================
ALTER TABLE manufacturers DROP COLUMN IF EXISTS created_at;
ALTER TABLE manufacturers DROP COLUMN IF EXISTS updated_at;
ALTER TABLE bike_models DROP COLUMN IF EXISTS created_at;

-- ============================================================
-- 10. 타입 마이그레이션 (INTEGER → BIGINT, Entity Long 필드 반영)
-- ddl-auto:update가 이미 BIGINT로 생성했으나, 수동 배포 시 필요
-- ============================================================
ALTER TABLE bikes ALTER COLUMN total_mileage_km TYPE BIGINT;
ALTER TABLE maintenances ALTER COLUMN mileage_at_maintenance TYPE BIGINT;
ALTER TABLE maintenances ALTER COLUMN cost TYPE BIGINT;
ALTER TABLE maintenances ALTER COLUMN next_due_km TYPE BIGINT;
ALTER TABLE maintenance_schedules ALTER COLUMN interval_km TYPE BIGINT;
ALTER TABLE fuelings ALTER COLUMN mileage_at_fueling TYPE BIGINT;
ALTER TABLE fuelings ALTER COLUMN price_per_liter TYPE BIGINT;
ALTER TABLE fuelings ALTER COLUMN total_cost TYPE BIGINT;

-- 제조사 초기 데이터는 data.sql에서 관리 (Hibernate 초기화 이후 실행)
