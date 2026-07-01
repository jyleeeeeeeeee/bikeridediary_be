# 오프라인 우선 sync API 스펙 (Phase 4)

앱이 로컬 SQLite를 진실의 원천으로 삼고, 온라인 상태가 되면 이 API로 서버에 upsert 하는 구조. 클라이언트가 생성한 UUID를 서버가 그대로 PK로 수용해야 함.

## 공통 원칙

- **ID 정책**: 클라이언트가 UUID v4 생성. 서버는 이 UUID를 리소스 PK로 사용.
- **Idempotency**: 같은 UUID로 여러 번 POST해도 항상 같은 결과. 이미 존재하면 update, 없으면 insert (upsert).
- **충돌 해결**: last-write-wins. 요청의 `updated_at`이 서버의 것보다 최신이면 반영, 아니면 skip (서버 응답에 실제 저장된 값 반환).
- **삭제 처리**: soft delete. `deleted_at` 필드를 요청에 포함. 서버는 삭제 상태로 저장, 후속 조회에서 제외.
- **인증**: 기존 JWT Bearer. 로컬 게스트는 서버 통신 없음 → 이 API 호출 안 함.
- **이미지 처리**: 정비 도메인만 해당. 이미지 자체는 별도 멀티파트로 업로드 후, sync 요청엔 URL 배열만 포함.

## 도메인별 엔드포인트

### 바이크 (`POST /api/v1/bikes/sync`)

**Request:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "manufacturerName": "혼다",
  "modelName": "CB1000R",
  "year": 2023,
  "bikeType": "네이키드",
  "totalMileageKm": 12500,
  "isRepresentative": true,
  "createdAt": "2026-06-30T14:22:11Z",
  "updatedAt": "2026-07-01T09:15:03Z",
  "deletedAt": null
}
```

**Response (200 OK):**
```json
{
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "manufacturerName": "혼다",
    ...
    "updatedAt": "2026-07-01T09:15:03Z"
  },
  "success": true
}
```

**서버 로직:**
1. `SELECT ... WHERE id = ? AND user_id = ?`
2. 없으면 INSERT (요청 값 그대로).
3. 있고 요청 `updated_at > 서버 updated_at`이면 UPDATE.
4. 있고 요청 `updated_at <= 서버 updated_at`이면 skip, 서버 값 반환.
5. 요청 `deleted_at != null`이면 soft delete 상태로 반영.

**BikeEntity 스키마 추가 컬럼:**
- `updated_at TIMESTAMP NOT NULL` (기존 있으면 그대로)
- `deleted_at TIMESTAMP NULL` — soft delete 마킹

**조회 시 filter**: `WHERE deleted_at IS NULL`을 모든 SELECT에 추가.

### 정비 (`POST /api/v1/maintenances/sync`)

바이크와 동일 패턴 + 이미지 URL 배열 유지. 이미지 파일 업로드는 기존 `POST /maintenances` 멀티파트 엔드포인트 재사용. sync 요청엔 이미 업로드된 URL 배열만 포함.

**Request 필드:**
```
id, bikeId, maintenanceType, mileageKm, cost, note, performedAt, imageUrls,
createdAt, updatedAt, deletedAt
```

**주의**: `bikeId` 참조 무결성 — 요청 순서상 바이크 sync가 먼저 완료되어야 함. 클라이언트가 순서 보장. 서버는 FK constraint deferred 또는 존재하지 않는 bikeId 요청 시 400 반환.

### 주유 (`POST /api/v1/fuelings/sync`)

정비와 동일 패턴. 이미지 없음.

**Request 필드:**
```
id, bikeId, fuelType, amountL, unitPricePerL, mileageKm, fueledAt,
createdAt, updatedAt, deletedAt
```

### 뱅킹 세션 (`POST /api/v1/banking-sessions/sync`)

기존 계획된 upload와 통합. clientSessionId(sqflite auto-increment)를 그대로 UUID 대신 사용해도 되지만, 일관성 위해 클라이언트 UUID 생성 권장.

**Request 필드:**
```
id (UUID), startedAt, endedAt, durationMs, maxLeftAngle, maxRightAngle,
avgAbsAngle, sampleCount, note, createdAt, updatedAt, deletedAt
```

샘플 데이터는 서버 저장 안 함 (요약만).

## 배치 vs 단건

각 도메인 sync는 단건 요청으로 처리. 배치가 필요해지면 향후 확장 (`POST /bikes/sync/batch`). MVP는 단건으로 충분.

## 에러 처리

- 400: request validation 실패, `bikeId` FK 없음, 필수 필드 누락
- 401: JWT 만료 → 앱이 refresh token으로 갱신 후 재시도
- 409: 낙관적 락 실패 (같은 리소스 동시 수정) — 클라이언트가 서버 응답 값으로 로컬 갱신
- 5xx: 서버 오류 → 앱이 sync_state = FAILED, sync_error에 사유 저장, 다음 사이클에 재시도

## 클라이언트 sync 흐름

1. `sync_state = PENDING` 로컬 레코드 조회
2. 도메인별 sync API 호출
3. 성공 → `sync_state = SYNCED`, `synced_at = now`, `sync_error = null`
4. 실패 → `sync_state = FAILED`, `sync_error = 사유`. 다음 트리거에 재시도
5. `deleted_at != null` + `SYNCED` → hard delete (로컬 정리)

## 구현 순서 권장

1. **바이크**부터 (참조 없음, 가장 단순)
2. **주유** (bikeId 참조, 이미지 없음)
3. **정비** (bikeId 참조 + 이미지)
4. **뱅킹 세션** (참조 없음, 이미 요약만 저장하므로 가장 작음)

각 도메인 완료 후 앱과 통합 검증.
