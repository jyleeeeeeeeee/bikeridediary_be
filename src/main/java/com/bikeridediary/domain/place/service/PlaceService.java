package com.bikeridediary.domain.place.service;

import com.bikeridediary.domain.place.dto.*;
import com.bikeridediary.domain.place.entity.PlaceEntity;
import com.bikeridediary.domain.place.repository.PlaceRepository;
import com.bikeridediary.domain.place_category.entity.PlaceCategoryEntity;
import com.bikeridediary.domain.place_category.repository.PlaceCategoryRepository;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
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

import static com.bikeridediary.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceService {

    private static final double DUPLICATE_RADIUS_METERS = 100.0;
    private static final BigDecimal LAT_DELTA_100M = new BigDecimal("0.0009"); // 100m ≈ 0.0009° 위도

    private final PlaceRepository placeRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final NaverSearchClient naverSearchClient;
    private final UserRepository userRepository;

    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final BigDecimal COORD_SCALE = BigDecimal.valueOf(10_000_000);

    @Transactional(readOnly = true)
    public List<PlaceResponse> list(String categoryCode) {
        return (
                (categoryCode == null || categoryCode.isBlank()) ?
                        placeRepository.findByDeletedAtIsNullOrderByPlaceCategoryEntity_DisplayOrderAsc() :
                        placeRepository.findByPlaceCategoryEntity_CategoryCodeAndDeletedAtIsNull(categoryCode)
        ).stream().map(PlaceResponse::from).toList();
    }

    @Transactional
    public PlaceResponse updateCoordinates(UUID id, CoordinateUpdateRequest request) {
        PlaceEntity place = placeRepository.findById(id).orElseThrow(
                () -> new BusinessException(PLACE_NOT_FOUND)
        );
        place.updateCoordinates(request);

        return PlaceResponse.from(place);
    }

    @Transactional
    public PlaceResponse updateInfo(UUID id, PlaceInfoUpdateRequest request) {
        PlaceEntity place = placeRepository.findById(id).orElseThrow(
                () -> new BusinessException(PLACE_NOT_FOUND)
        );

        PlaceCategoryEntity placeCategory = placeCategoryRepository.findById(request.category()).orElseThrow(
                () -> new BusinessException(PLACE_CATEGORY_NOT_FOUND)
        );

        place.updateInfo(request.placeName(), placeCategory);
        return PlaceResponse.from(place);
    }

    public List<PlaceCandidateResponse> searchExternal(String query) {
        if (query == null || query.isBlank()) return List.of();

        NaverLocalSearchResponse res = naverSearchClient.search(query.trim());
        if (res == null || res.items() == null) return List.of();

        return res.items().stream()
                .map(this::toCandidate)
                .toList();
    }

    public PlaceResponse addNewPlace(PlaceInsertRequest request, UUID userId) {
        UserEntity userEntity = verifyUserExists(userId);
        String placeName = request.placeName();
        BigDecimal lat = request.latitude();
        BigDecimal lng = request.longitude();

        // 1단계: bounding box (100m 반경을 감싸는 사각형)
        // 위도는 어디서나 1° ≈ 111km 상수
        // 경도는 cos(위도) 만큼 축소 (한국 37°N 기준 0.0009 / cos(37°) ≈ 0.00113°)
        BigDecimal cosLat = BigDecimal.valueOf(Math.cos(Math.toRadians(lat.doubleValue())));
        BigDecimal lngDelta = LAT_DELTA_100M.divide(cosLat, 10, RoundingMode.HALF_UP);

        List<PlaceEntity> candidates = placeRepository.findNearbyByName(
                placeName,
                lat.subtract(LAT_DELTA_100M),
                lat.add(LAT_DELTA_100M),
                lng.subtract(lngDelta),
                lng.add(lngDelta)
        );

        // 2단계: Haversine으로 실제 거리 확인 (사각형 → 원 정밀화)
        boolean duplicate = candidates.stream().anyMatch(p ->
                haversineMeters(lat, lng, p.getLatitude(), p.getLongitude())
                        < DUPLICATE_RADIUS_METERS
        );
        if (duplicate) {
            throw new BusinessException(PLACE_ALREADY_EXIST);
        }


        PlaceCategoryEntity placeCategoryEntity = placeCategoryRepository.findById(request.category())
                .orElse(PlaceCategoryEntity.create("OTHER", "기타", 9999));

        PlaceEntity placeEntity = PlaceEntity.create(
                placeName,
                userEntity,
                placeCategoryEntity,
                request.latitude(),
                request.longitude(),
                request.address(),
                request.roadAddress(),
                request.description(),
                request.photoUrl(),
                request.phone(),
                null,
                null
        );

        PlaceEntity saved = placeRepository.save(placeEntity);
        return PlaceResponse.from(saved);
    }

    private PlaceCandidateResponse toCandidate(NaverLocalItem item) {
        BigDecimal lng = new BigDecimal(item.mapx())
                .divide(COORD_SCALE, 7, RoundingMode.HALF_UP);
        BigDecimal lat = new BigDecimal(item.mapy())
                .divide(COORD_SCALE, 7, RoundingMode.HALF_UP);

        return new PlaceCandidateResponse(
                stripHtmlTags(item.title()),
                item.category(),
                lat,
                lng,
                nullIfBlank(item.address()),
                nullIfBlank(item.roadAddress())
        );
    }

    private static String stripHtmlTags(String s) {
        if (s == null) return null;
        return HTML_TAG.matcher(s).replaceAll("").trim();
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private UserEntity verifyUserExists(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 두 좌표 사이의 대원 거리 (미터).
     * 지구 반경 6371km 기준 Haversine 공식.
     */
    private static double haversineMeters(
            BigDecimal lat1, BigDecimal lng1,
            BigDecimal lat2, BigDecimal lng2
    ) {
        final double R = 6_371_000; // 지구 반경 (m)
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
        double dLng = Math.toRadians(lng2.subtract(lng1).doubleValue());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }


}
