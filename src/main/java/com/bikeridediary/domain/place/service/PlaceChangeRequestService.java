package com.bikeridediary.domain.place.service;

import com.bikeridediary.domain.place.dto.*;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestEntity;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestStatus;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestType;
import com.bikeridediary.domain.place.entity.PlaceEntity;
import com.bikeridediary.domain.place.repository.PlaceChangeRequestRepository;
import com.bikeridediary.domain.place.repository.PlaceRepository;
import com.bikeridediary.domain.placecategory.entity.PlaceCategoryEntity;
import com.bikeridediary.domain.placecategory.repository.PlaceCategoryRepository;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.entity.UserRole;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import static com.bikeridediary.global.exception.ErrorCode.*;
import com.bikeridediary.global.response.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static com.bikeridediary.domain.place.entity.PlaceChangeRequestStatus.*;
import static com.bikeridediary.domain.place.entity.PlaceChangeRequestType.*;


@Service
@RequiredArgsConstructor
public class PlaceChangeRequestService {
    // 요청자별 PENDING CREATE 요청 상한 (스팸 방어)
    private static final long MAX_PENDING_PER_USER = 20;

    private final PlaceChangeRequestRepository placeChangeRequestRepository;
    private final PlaceRepository placeRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ============================================================
    // 일반 유저 API
    // ============================================================

    // 요청 생성
    @Transactional
    public PlaceChangeRequestResponse create(UUID requesterId, PlaceChangeRequestCreateRequest request) {
        UserEntity requester = userRepository.findByIdAndDeletedAtIsNull(requesterId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        boolean isAdmin = requester.getRole().equals(UserRole.ADMIN);

        validatePayload(request.type(), request.payload(), request.targetPlaceId());

        PlaceEntity targetPlace = null;
        if (request.type() != CREATE) {
            targetPlace = placeRepository.findById(request.targetPlaceId())
                    .orElseThrow(() -> new BusinessException(PLACE_NOT_FOUND));
            if (!isAdmin && placeChangeRequestRepository.existsByTargetPlace_IdAndStatus(
                                request.targetPlaceId(), PENDING)) {
                throw new BusinessException(PLACE_REQUEST_ALREADY_PENDING);
            }

        } else if(!isAdmin){
            long pending = placeChangeRequestRepository.countByRequester_IdAndStatus(requesterId, PENDING);
            if (pending >= MAX_PENDING_PER_USER) {
                throw new BusinessException(PLACE_REQUEST_LIMIT_EXCEEDED);
            }
        }

        PlaceChangeRequestEntity saved = placeChangeRequestRepository.save(
                PlaceChangeRequestEntity.create(request.type(), targetPlace, requester, request.payload())
        );

        if(isAdmin) {
            applyToPlaces(saved);
            saved.approve(requester, null);
        }

        return PlaceChangeRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<PlaceChangeRequestResponse> listMine(UUID requesterId, Pageable pageable) {
        return PageResponse.of(
                placeChangeRequestRepository.findByRequester_Id(requesterId, pageable),
                PlaceChangeRequestResponse::from
        );
    }

    // ============================================================
    // 어드민 API
    // ============================================================

    // 요청 상세 (상태 필터, 기본 PENDING)
    @Transactional(readOnly = true)
    public AdminPlaceChangeRequestResponse infoForAdmin(UUID id) {
        PlaceChangeRequestEntity e = placeChangeRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(RESOURCE_NOT_FOUND));

        return AdminPlaceChangeRequestResponse.from(e);
    }

    // 어드민 목록 (상태 필터, 기본 PENDING) — 페이징
    @Transactional(readOnly = true)
    public PageResponse<AdminPlaceChangeRequestResponse> listForAdmin(
            PlaceChangeRequestStatus status, Pageable pageable) {
        PlaceChangeRequestStatus effective = status == null ? PlaceChangeRequestStatus.PENDING : status;
        return PageResponse.of(
                placeChangeRequestRepository.findByStatus(effective, pageable),
                AdminPlaceChangeRequestResponse::from
        );
    }

    // 어드민 승인 - 트랜잭션 내에서 places 반영 후 status 변경
    @Transactional
    public AdminPlaceChangeRequestResponse approve(UUID requestId, UUID reviewerId, AdminReviewRequest review) {
        PlaceChangeRequestEntity request = placeChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(PLACE_REQUEST_NOT_FOUND));
        if (request.getStatus() != PlaceChangeRequestStatus.PENDING) {
            throw new BusinessException(PLACE_REQUEST_ALREADY_REVIEWED);
        }
        UserEntity reviewer = userRepository.findByIdAndDeletedAtIsNull(reviewerId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        applyToPlaces(request);
        request.approve(reviewer, review == null ? null : review.note());
        return AdminPlaceChangeRequestResponse.from(request);
    }

    // 어드민 거절
    @Transactional
    public AdminPlaceChangeRequestResponse reject(UUID requestId, UUID reviewerId, AdminReviewRequest review) {
        PlaceChangeRequestEntity request = placeChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(PLACE_REQUEST_NOT_FOUND));
        if (request.getStatus() != PlaceChangeRequestStatus.PENDING) {
            throw new BusinessException(PLACE_REQUEST_ALREADY_REVIEWED);
        }
        UserEntity reviewer = userRepository.findByIdAndDeletedAtIsNull(reviewerId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        request.reject(reviewer, review == null ? null : review.note());
        return AdminPlaceChangeRequestResponse.from(request);
    }

    // ============================================================
    // Private
    // ============================================================

    // type별 payload → 실제 places 반영
    private void applyToPlaces(PlaceChangeRequestEntity request) {
        switch (request.getType()) {
            case CREATE -> {
                CreatePlaceRequestPayload p = objectMapper.convertValue(
                        request.getPayload(), CreatePlaceRequestPayload.class);

                if (placeRepository.existsById(p.clientUuid())) {
                    throw new BusinessException(PLACE_ALREADY_EXIST);
                }

                PlaceCategoryEntity category = placeCategoryRepository.findById(p.category())
                        .orElseThrow(() -> new BusinessException(PLACE_CATEGORY_NOT_FOUND));

                // 앱이 생성한 clientUuid를 그대로 places.id로 (D9=A)
                PlaceEntity created = PlaceEntity.createWithId(
                        p.clientUuid(),
                        p.placeName(),
                        request.getRequester(),
                        category,
                        p.latitude(),
                        p.longitude(),
                        p.address(),
                        p.roadAddress(),
                        p.description(),
                        p.photoUrl(),
                        p.phone(),
                        null,
                        null
                );
                placeRepository.save(created);
            }
            case UPDATE_COORDINATES -> {
                UpdateCoordinatesPayload p = objectMapper.convertValue(
                        request.getPayload(), UpdateCoordinatesPayload.class);
                request.getTargetPlace().updateCoordinates(
                        p.latitude(), p.longitude());
            }
            case UPDATE_INFO -> {
                UpdateInfoPayload p = objectMapper.convertValue(
                        request.getPayload(), UpdateInfoPayload.class);
                PlaceCategoryEntity category = placeCategoryRepository.findById(p.category())
                        .orElseThrow(() -> new BusinessException(PLACE_CATEGORY_NOT_FOUND));
                request.getTargetPlace().updateInfo(p.placeName(), category, p.description());
            }
        }
    }


    // type별 payload 형태 최소 검증
    private void validatePayload(PlaceChangeRequestType type, Map<String, Object> payload, UUID targetPlaceId) {
        if (payload == null) {
            throw new BusinessException(INVALID_INPUT);
        }
        switch (type) {
            case CREATE -> {
                if (targetPlaceId != null) throw new BusinessException(INVALID_INPUT);
                CreatePlaceRequestPayload p = objectMapper.convertValue(payload, CreatePlaceRequestPayload.class);// 필드 매핑 실패 시 예외

                if(p.clientUuid() == null) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                if(p.placeName() == null || p.placeName().trim().isBlank()) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                if(p.category() == null || p.category().trim().isBlank()) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                if(p.latitude() == null || p.longitude() == null) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                if(p.latitude().abs().compareTo(BigDecimal.valueOf(90)) > 0) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                if(p.longitude().abs().compareTo(BigDecimal.valueOf(180)) > 0) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
            }
            case UPDATE_COORDINATES -> {
                if (targetPlaceId == null) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                UpdateCoordinatesPayload p = objectMapper.convertValue(payload, UpdateCoordinatesPayload.class);

                if(p.latitude() == null || p.longitude() == null) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                if(p.latitude().abs().compareTo(BigDecimal.valueOf(90)) > 0) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                if(p.longitude().abs().compareTo(BigDecimal.valueOf(180)) > 0) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
            }
            case UPDATE_INFO -> {
                if (targetPlaceId == null) throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                UpdateInfoPayload p = objectMapper.convertValue(payload, UpdateInfoPayload.class);
                if (p.placeName() == null || p.placeName().isBlank() || p.category() == null) {
                    throw new BusinessException(INVALID_REQUEST_PAYLOAD);
                }
            }
        }
    }

}
