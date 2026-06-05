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
    last_maintenance_mileage INTEGER,
    last_maintenance_date    DATE,
    created_at               TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP,
    deleted_at               TIMESTAMP
);

-- ============================================================
-- 5. 인덱스
-- ============================================================

-- bikes: findByUserIdAndDeletedAtIsNullOrderByIsRepresentativeDescCreatedAtDesc
CREATE INDEX idx_bikes_user_id_deleted_at ON bikes (user_id, deleted_at);

-- maintenances: findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc
CREATE INDEX idx_maintenances_bike_id_deleted_at ON maintenances (bike_id, deleted_at);

-- maintenance_schedules: findByBikeEntityIdAndDeletedAtIsNull
CREATE INDEX idx_maintenance_schedules_bike_id_deleted_at ON maintenance_schedules (bike_id, deleted_at);

-- maintenance_schedules: existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull (중복 체크)
CREATE INDEX idx_maintenance_schedules_bike_id_type_deleted_at ON maintenance_schedules (bike_id, maintenance_type, deleted_at);
