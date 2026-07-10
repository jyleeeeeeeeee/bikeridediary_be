# Place 도메인 API 스펙

라이더용 큐레이션 POI (명소/바이크 카페/정비 센터). 주유소는 별도 `station` 도메인 유지.

## 패키지 구조

```
com.bikeridediary.domain.place
├── controller/PlaceController.java
├── service/PlaceService.java
├── repository/PlaceRepository.java
├── entity/PlaceEntity.java
├── dto/
│   ├── PlaceResponse.java
│   └── PlaceListRequest.java
└── PlaceCategory.java   (enum)
```

## PlaceCategory (enum)

```java
public enum PlaceCategory {
    LANDMARK,        // 명소
    CAFE,            // 바이크 카페
    SERVICE_CENTER   // 정비 센터
}
```

## PlaceEntity 필드 (BaseEntity 상속)

| 필드 | 타입 | 비고 |
|-----|------|-----|
| id | UUID | PK |
| name | String(100) | 장소 이름 |
| category | PlaceCategory | @Enumerated(STRING) |
| latitude | double | 위도 |
| longitude | double | 경도 |
| address | String(200) | 도로명 주소 |
| description | @Column(columnDefinition="TEXT") String | 큐레이션 설명 |
| photoUrl | String | 대표 사진 URL |
| phone | String(30) | nullable |
| kakaoPlaceId | String(50) | 카카오맵 딥링크용, nullable |
| naverPlaceId | String(50) | 네이버 지도 딥링크용, nullable |

BaseEntity 상속으로 createdAt/updatedAt/deletedAt 자동 관리.

## Repository

```java
public interface PlaceRepository extends JpaRepository<PlaceEntity, UUID> {

    List<PlaceEntity> findByCategoryAndDeletedAtIsNull(PlaceCategory category);

    List<PlaceEntity> findByDeletedAtIsNullOrderByCategoryAsc();

    // 지도 bbox 검색 (선택, 데이터 많아지면 도입)
    @Query("""
        SELECT p FROM PlaceEntity p
        WHERE p.deletedAt IS NULL
          AND (:category IS NULL OR p.category = :category)
          AND p.latitude BETWEEN :swLat AND :neLat
          AND p.longitude BETWEEN :swLng AND :neLng
    """)
    List<PlaceEntity> findInBbox(
        @Param("category") PlaceCategory category,
        @Param("swLat") double swLat, @Param("neLat") double neLat,
        @Param("swLng") double swLng, @Param("neLng") double neLng
    );
}
```

## Controller 엔드포인트

```java
@Tag(name = "장소", description = "라이더 큐레이션 POI (명소/카페/정비센터)")
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 목록 조회 (전체 또는 카테고리 필터)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> list(
            @RequestParam(required = false) PlaceCategory category
    ) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.list(category)));
    }

    @Operation(summary = "장소 상세 조회")
    @GetMapping("/{placeId}")
    public ResponseEntity<ApiResponse<PlaceResponse>> get(@PathVariable UUID placeId) {
        return ResponseEntity.ok(ApiResponse.ok(placeService.get(placeId)));
    }
}
```

인증 필요 여부: 로그인/게스트 모두 조회 가능 → SecurityConfig에서 `/api/v1/places/**`을 `permitAll()`로 설정하거나 authenticated 유지(로컬 게스트에게도 노출하려면 permitAll 권장).

## PlaceResponse DTO

```java
public record PlaceResponse(
    UUID id,
    String name,
    PlaceCategory category,
    double latitude,
    double longitude,
    String address,
    String description,
    String photoUrl,
    String phone,
    String kakaoPlaceId,
    String naverPlaceId
) {
    public static PlaceResponse from(PlaceEntity entity) {
        return new PlaceResponse(
            entity.getId(), entity.getName(), entity.getCategory(),
            entity.getLatitude(), entity.getLongitude(),
            entity.getAddress(), entity.getDescription(),
            entity.getPhotoUrl(), entity.getPhone(),
            entity.getKakaoPlaceId(), entity.getNaverPlaceId()
        );
    }
}
```

## 시드 데이터 (schema/data.sql 또는 별도 place-seed.sql)

초기 5~10개로 시작. 예시:

```sql
INSERT INTO places (id, name, category, latitude, longitude, address, description, created_at, updated_at) VALUES
('a1000000-0000-0000-0000-000000000001', '북악 스카이웨이 팔각정', 'LANDMARK', 37.6076, 126.9797, '서울 종로구 북악산로 267', '서울 시내를 한눈에 내려다볼 수 있는 라이더 성지. 야경 명소.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000002', '남산 팔각정', 'LANDMARK', 37.5510, 126.9882, '서울 용산구 남산공원길 105', '남산 정상 명소. 저녁 라이딩 목적지로 인기.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000003', '가평 아침고요 라이더 카페', 'CAFE', 37.7523, 127.4681, '경기 가평군 상면 수목원로 432', '2층 통유리 뷰. 대형 주차장, 헬멧 거치대 완비.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000004', '양평 두물머리 카페', 'CAFE', 37.5372, 127.3236, '경기 양평군 양서면 양수리 683-1', '한강 뷰. 주말 라이더 모임 장소.', NOW(), NOW()),
('a1000000-0000-0000-0000-000000000005', '분당 바이크월드 정비센터', 'SERVICE_CENTER', 37.3595, 127.1052, '경기 성남시 분당구 정자일로 95', '대형 바이크 종합 정비. 예약 필요.', NOW(), NOW());
```

## SecurityConfig 수정

```java
.requestMatchers(HttpMethod.GET, "/api/v1/places/**").permitAll()
```
`/api/v1/auth/**`, `/api/v1/stations/**` 옆에 추가. 로컬 게스트에게도 지도 표시 가능.

## 구현 순서 권장

1. `PlaceCategory` enum
2. `PlaceEntity` (BaseEntity 상속)
3. `PlaceRepository`
4. `PlaceResponse` DTO
5. `PlaceService.list()`, `PlaceService.get()`
6. `PlaceController`
7. SecurityConfig permitAll 추가
8. schema.sql에 `places` 테이블 + place-seed.sql 시드 데이터
9. Swagger UI에서 `GET /api/v1/places` 확인
