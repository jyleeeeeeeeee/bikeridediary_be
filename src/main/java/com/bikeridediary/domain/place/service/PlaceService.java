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
        PlaceCategoryEntity placeCategoryEntity = placeCategoryRepository.findById(request.category())
                .orElse(PlaceCategoryEntity.create("OTHER", "기타", 9999));
        PlaceEntity placeEntity = PlaceEntity.create(
                request.placeName(),
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

}
