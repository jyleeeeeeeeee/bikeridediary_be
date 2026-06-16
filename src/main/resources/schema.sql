-- ============================================================
-- BikeRideDiary (바라다) PostgreSQL 스키마
-- 대상 Entity: UserEntity, BikeEntity, MaintenanceEntity, MaintenanceScheduleEntity
-- RefreshToken은 Redis에 저장되므로 PostgreSQL 스키마에 포함하지 않음
-- ============================================================

-- UUID 생성 함수 활성화 (PostgreSQL 13 이상은 기본 내장)
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. users (사용자)
-- ============================================================
CREATE TABLE users (
    id                UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    provider          VARCHAR(20)  NOT NULL,
    provider_id       VARCHAR(255) NOT NULL,
    email             VARCHAR(255),
    password          VARCHAR(255),
    nickname          VARCHAR(50)  NOT NULL,
    profile_image_url TEXT,
    fcm_token         TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP,

    CONSTRAINT uq_users_provider UNIQUE (provider, provider_id)
);

-- ============================================================
-- 2. bikes (바이크)
-- ============================================================
CREATE TABLE bikes (
    id                UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id           UUID         NOT NULL REFERENCES users(id),
    manufacturer_name VARCHAR(100) NOT NULL,
    model_name        VARCHAR(100) NOT NULL,
    year              INTEGER      NOT NULL,
    category          VARCHAR(20),
    total_mileage_km  INTEGER      NOT NULL DEFAULT 0,
    is_representative BOOLEAN      NOT NULL DEFAULT FALSE,
    purchased_at      DATE,
    photo_url         TEXT,
    memo              VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

-- ============================================================
-- 3. maintenances (정비 이력)
-- ============================================================
CREATE TABLE maintenances (
    id                     UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id                UUID         NOT NULL REFERENCES bikes(id),
    maintenance_type       VARCHAR(20)  NOT NULL,
    maintenance_date       DATE         NOT NULL,
    mileage_at_maintenance INTEGER      NOT NULL,
    cost                   INTEGER,
    description            VARCHAR(500),
    next_due_km            INTEGER,
    next_due_date          DATE,
    created_at             TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at             TIMESTAMP,
    deleted_at             TIMESTAMP
);

-- ============================================================
-- 4. maintenance_schedules (정비 주기)
-- ============================================================
CREATE TABLE maintenance_schedules (
    id                       UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id                  UUID         NOT NULL REFERENCES bikes(id),
    maintenance_type         VARCHAR(20)  NOT NULL,
    interval_km              INTEGER,
    interval_months          INTEGER,
    created_at               TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP,
    deleted_at               TIMESTAMP
);

-- ============================================================
-- 5. fuelings (주유 기록)
-- ============================================================
CREATE TABLE fuelings (
    id                UUID           DEFAULT gen_random_uuid() PRIMARY KEY,
    bike_id           UUID           NOT NULL REFERENCES bikes(id),
    fueling_date      DATE           NOT NULL,
    mileage_at_fueling INTEGER       NOT NULL,
    fuel_amount       NUMERIC(8,2)   NOT NULL,
    price_per_liter   INTEGER,
    total_cost        INTEGER,
    fuel_type         VARCHAR(10)    NOT NULL,
    is_full_tank      BOOLEAN        NOT NULL DEFAULT FALSE,
    fuel_efficiency   NUMERIC(6,2),
    memo              VARCHAR(500),
    station_name      VARCHAR(100),
    created_at        TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP
);

-- ============================================================
-- 6. 인덱스
-- ============================================================

-- bikes: findByUserEntityIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc
CREATE INDEX idx_bikes_user_id_deleted_at ON bikes (user_id, deleted_at);
-- bikes: findByUserEntityIdAndIsRepresentativeTrueAndDeletedAtIsNull
CREATE INDEX idx_bikes_user_id_representative_deleted_at ON bikes (user_id, is_representative, deleted_at);

-- maintenances: findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc
CREATE INDEX idx_maintenances_bike_id_deleted_at ON maintenances (bike_id, deleted_at);
-- maintenances: findByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNullOrderByMaintenanceDateDesc
CREATE INDEX idx_maintenances_bike_id_type_deleted_at ON maintenances (bike_id, maintenance_type, deleted_at);

-- maintenance_schedules: findByBikeEntityIdAndDeletedAtIsNull
CREATE INDEX idx_maintenance_schedules_bike_id_deleted_at ON maintenance_schedules (bike_id, deleted_at);
-- maintenance_schedules: existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull (중복 체크)
CREATE INDEX idx_maintenance_schedules_bike_id_type_deleted_at ON maintenance_schedules (bike_id, maintenance_type, deleted_at);

-- fuelings: findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDesc
CREATE INDEX idx_fuelings_bike_id_deleted_at ON fuelings (bike_id, deleted_at);
-- fuelings: 만탱크법 연비 계산용 (이전 만탱크 기록 조회)
CREATE INDEX idx_fuelings_bike_id_full_tank_mileage ON fuelings (bike_id, is_full_tank, mileage_at_fueling, deleted_at);
