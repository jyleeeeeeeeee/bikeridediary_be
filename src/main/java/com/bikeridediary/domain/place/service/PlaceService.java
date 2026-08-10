package com.bikeridediary.domain.place.service;

import com.bikeridediary.domain.place.dto.*;
import com.bikeridediary.domain.place.entity.PlaceEntity;
import com.bikeridediary.domain.place.repository.PlaceRegistrationCount;
import com.bikeridediary.domain.place.repository.PlaceRepository;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import static com.bikeridediary.global.exception.ErrorCode.*;
import com.bikeridediary.infra.naver.maps.NaverMapsClient;
import com.bikeridediary.infra.naver.maps.dto.NaverGeocodeResponse;
import com.bikeridediary.infra.naver.search.NaverSearchClient;
import com.bikeridediary.infra.naver.search.dto.NaverLocalItem;
import com.bikeridediary.infra.naver.search.dto.NaverLocalSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;



@Service
@RequiredArgsConstructor
public class PlaceService {

    private static final double DUPLICATE_RADIUS_METERS = 100.0;
    private static final BigDecimal LAT_DELTA_100M = new BigDecimal("0.0009"); // 100m ≈ 0.0009° 위도

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final NaverSearchClient naverSearchClient;
    private final NaverMapsClient naverMapsClient;

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final BigDecimal COORD_SCALE = BigDecimal.valueOf(10_000_000);

    @Transactional(readOnly = true)
    public List<PlaceResponse> list(String categoryCode) {
        return (
                (categoryCode == null || categoryCode.isBlank()) ?
                    placeRepository.findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc() :
                    placeRepository.findByPlaceCategoryEntity_CategoryCodeAndDeletedAtIsNull(categoryCode))
                .stream().map(PlaceResponse::from).toList();
    }

    // Naver 지역검색 API는 display 최대 5 상한이라 페이지 크기 고정.
    // 앱은 다음 페이지 필요 시 start=6, 11, 16...으로 요청 (무한 스크롤).
    public List<PlaceCandidateResponse> searchExternal(String query, int start) {
        if (query == null || query.isBlank()) return List.of();
        if (start < 1) start = 1;

        NaverLocalSearchResponse response = naverSearchClient.search(query.trim(), start);
        if (response == null || response.items() == null) return List.of();

        return response.items().stream().map(this::toCandidate).toList();
    }

    public List<GeocodeResultResponse> geocode(String query) {
        NaverGeocodeResponse response = naverMapsClient.search(query);
        if (response == null || !"OK".equals(response.status()) ||
                response.addresses() == null || response.addresses().isEmpty()) {
            return List.of();
        }

        return response.addresses().stream()
                .map(GeocodeResultResponse::from)
                .toList();
    }


    private PlaceCandidateResponse toCandidate(NaverLocalItem item) {
        BigDecimal lng = new BigDecimal(item.mapx()).divide(COORD_SCALE, 7, RoundingMode.HALF_UP);
        BigDecimal lat = new BigDecimal(item.mapy()).divide(COORD_SCALE, 7, RoundingMode.HALF_UP);

        return new PlaceCandidateResponse(stripHtmlTags(item.title()), item.category(), lat, lng, nullIfBlank(item.address()), nullIfBlank(item.roadAddress()));
    }

    private static String stripHtmlTags(String s) {
        if (s == null) return null;
        return HTML_TAG.matcher(s).replaceAll("").trim();
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // 유저별 장소 등록 순위 (내림차순)
    @Transactional(readOnly = true)
    public List<PlaceRankingResponse> getRankings() {
        List<PlaceRegistrationCount> counts = placeRepository.countRegistrationsByUser();
        List<PlaceRankingResponse> ranked = new java.util.ArrayList<>(counts.size());
        for (int i = 0; i < counts.size(); i++) {
            ranked.add(PlaceRankingResponse.of(i + 1, counts.get(i)));
        }
        return ranked;
    }

    // 어드민 soft delete — deleted_at 세팅. 지도/조회에서 숨김. 복구 가능.
    @Transactional
    public void softDeleteByAdmin(UUID placeId) {
        PlaceEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(PLACE_NOT_FOUND));
        if (place.getDeletedAt() != null) return; // 이미 삭제됨 (idempotent)
        place.delete(); // BaseEntity.delete() — deletedAt = now
    }

    // 어드민 hard delete — 완전 삭제. FK CASCADE로 관련 데이터 함께 삭제:
    //   - place_wishes: 삭제
    //   - place_change_requests: 삭제 (제보 이력 소실 주의)
    //   - course_waypoints.place_id: SET NULL (waypoint 자체는 유지)
    @Transactional
    public void hardDeleteByAdmin(UUID placeId) {
        PlaceEntity place = placeRepository.findById(placeId)
                .orElseThrow(() -> new BusinessException(PLACE_NOT_FOUND));
        placeRepository.delete(place);
    }

    private UserEntity verifyUserExists(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    /**
     * 두 좌표 사이의 대원 거리 (미터).
     * 지구 반경 6371km 기준 Haversine 공식.
     */
    private static double haversineMeters(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        final double R = 6_371_000; // 지구 반경 (m)
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double dLng = Math.toRadians(lng2.subtract(lng1).doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Transactional(readOnly = true)
    public void checkDuplicate(String placeName, BigDecimal latitude, BigDecimal longitude) {

        // 1단계: bounding box (100m 반경을 감싸는 사각형)
        // 위도는 어디서나 1° ≈ 111km 상수
        // 경도는 cos(위도) 만큼 축소 (한국 37°N 기준 0.0009 / cos(37°) ≈ 0.00113°)
        BigDecimal cosLat = BigDecimal.valueOf(Math.cos(Math.toRadians(latitude.doubleValue())));
        BigDecimal lngDelta = LAT_DELTA_100M.divide(cosLat, 10, RoundingMode.HALF_UP);

        List<PlaceEntity> candidates = placeRepository.findNearbyByName(placeName, latitude.subtract(LAT_DELTA_100M), latitude.add(LAT_DELTA_100M), longitude.subtract(lngDelta), longitude.add(lngDelta));

        // 2단계: Haversine으로 실제 거리 확인 (사각형 → 원 정밀화)
        boolean duplicate = candidates.stream().anyMatch(p -> haversineMeters(latitude, longitude, p.getLatitude(), p.getLongitude()) < DUPLICATE_RADIUS_METERS);
        if (duplicate) {
            throw new BusinessException(PLACE_ALREADY_EXIST);
        }
    }

}
