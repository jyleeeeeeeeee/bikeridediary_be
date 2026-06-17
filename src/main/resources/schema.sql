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
-- 6. manufacturers (제조사 마스터)
-- ============================================================
CREATE TABLE manufacturers (
    id               BIGSERIAL    PRIMARY KEY,
    api_name         VARCHAR(100) NOT NULL UNIQUE,
    display_name_ko  VARCHAR(100) NOT NULL,
    country          VARCHAR(50),
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order    INTEGER      NOT NULL DEFAULT 999,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP
);

-- ============================================================
-- 7. bike_models (바이크 모델 마스터)
-- ============================================================
CREATE TABLE bike_models (
    id               BIGSERIAL    PRIMARY KEY,
    manufacturer_id  BIGINT       NOT NULL REFERENCES manufacturers(id),
    name             VARCHAR(150) NOT NULL,
    year             INTEGER,
    type             VARCHAR(50),
    displacement     VARCHAR(100),
    engine           VARCHAR(200),
    power            VARCHAR(150),
    torque           VARCHAR(150),
    total_weight     VARCHAR(100),
    seat_height      VARCHAR(100),
    fuel_capacity    VARCHAR(100),
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uq_bike_models_mfr_name_year UNIQUE (manufacturer_id, name, year)
);

-- ============================================================
-- 8. 인덱스
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

-- bike_models: 제조사별 모델 조회
CREATE INDEX idx_bike_models_manufacturer_id ON bike_models (manufacturer_id);

-- ============================================================
-- 9. 제조사 초기 데이터 (API-Ninjas 검증 완료)
-- ============================================================
INSERT INTO manufacturers (api_name, display_name_ko, country, display_order) VALUES
('Honda',            '혼다',         '일본',     1),
('Yamaha',           '야마하',       '일본',     2),
('Kawasaki',         '가와사키',     '일본',     3),
('Suzuki',           '스즈키',       '일본',     4),
('BMW',              'BMW',          '독일',     5),
('Ducati',           '두카티',       '이탈리아', 6),
('KTM',              'KTM',          '오스트리아', 7),
('Harley-Davidson',  '할리데이비슨', '미국',     8),
('Triumph',          '트라이엄프',   '영국',     9),
('Aprilia',          '아프릴리아',   '이탈리아', 10),
('Husqvarna',        '허스크바나',   '스웨덴',   11),
('Indian',           '인디안',       '미국',     12),
('Moto Guzzi',       '모토구찌',     '이탈리아', 13),
('MV Agusta',        'MV 아구스타',  '이탈리아', 14),
('Vespa',            '베스파',       '이탈리아', 15),
('Benelli',          '베넬리',       '이탈리아', 16),
('Peugeot',          '푸조',         '프랑스',   17),
('kymco',            '킴코',         '대만',     18),
('sym',              'SYM',          '대만',     19),
('Daelim',           '대림',         '한국',     20),
('Piaggio',          '피아지오',     '이탈리아', 21),
('CF Moto',          'CF모토',       '중국',     22),
('Bajaj',            '바자즈',       '인도',     23),
('Hero',             '히어로',       '인도',     24),
('TVS',              'TVS',          '인도',     25),
('Hyosung',          '효성',         '한국',     26),
('Can-Am',           '캔암',         '캐나다',   27),
('Zero',             '제로',         '미국',     28),
('Gas Gas',          '가스가스',     '스페인',   29),
('Beta',             '베타',         '이탈리아', 30),
('Sherco',           '셔코',         '프랑스',   31),
('Bimota',           '비모타',       '이탈리아', 32),
('Norton',           '노튼',         '영국',     33),
('Buell',            '뷰엘',         '미국',     34),
('Energica',         '에너지카',     '이탈리아', 35),
('Cagiva',           '카지바',       '이탈리아', 36),
('SWM',              'SWM',          '이탈리아', 37),
('Voge',             '보쥬',         '중국',     38),
('Zontes',           '존테스',       '중국',     39),
('Jawa',             '자와',         '체코',     40),
('Keeway',           '키위',         '헝가리',   41),
('Lifan',            '리판',         '중국',     42),
('Mash',             '매쉬',         '프랑스',   43),
('QJMotor',          'QJ모터',       '중국',     44),
('Derbi',            '데르비',       '스페인',   45),
('Ural',             '우랄',         '러시아',   46),
('TM',               'TM',           '이탈리아', 47)
ON CONFLICT (api_name) DO NOTHING;
