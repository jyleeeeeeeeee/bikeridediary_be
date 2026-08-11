package com.bikeridediary.domain.course.service;

import com.bikeridediary.domain.course.dto.*;
import com.bikeridediary.domain.course.entity.CourseEntity;
import com.bikeridediary.domain.course.entity.CourseFavoriteEntity;
import com.bikeridediary.domain.course.entity.CourseFavoriteId;
import com.bikeridediary.domain.course.entity.CourseWaypointEntity;
import com.bikeridediary.domain.course.repository.CourseFavoriteRepository;
import com.bikeridediary.domain.course.repository.CourseRepository;
import com.bikeridediary.domain.course.repository.CourseWaypointRepository;
import com.bikeridediary.domain.place.entity.PlaceEntity;
import com.bikeridediary.domain.place.repository.PlaceRepository;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.response.PageResponse;
import com.bikeridediary.infra.naver.maps.NaverMapsClient;
import com.bikeridediary.infra.naver.maps.dto.NaverDirectionsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.bikeridediary.global.exception.ErrorCode.*;
import static com.bikeridediary.infra.naver.maps.NaverMapsClient.*;
import static com.bikeridediary.infra.naver.maps.dto.NaverDirectionsResponse.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseFavoriteRepository courseFavoriteRepository;
    private final CourseWaypointRepository courseWaypointRepository;
    private final PlaceRepository placeRepository;
    private final NaverMapsClient naverMapsClient;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private static final Set<String> WAYPOINT_ROLE = Set.of("START", "GOAL", "VIA");

    // 내가 만든 코스 (페이징) — 항상 isMineOnly=true 로 매핑되므로 isFavorited=false
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getMyOwnedCourses(UUID userId, Pageable pageable) {
        return PageResponse.of(
                courseRepository.findByUserEntityId(userId, pageable),
                courseEntity -> CourseSummaryResponse.from(courseEntity, userId, false)
        );
    }

    // 내가 즐겨찾기한 코스 (Slice) — 무한 스크롤용, count 절약
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getMyFavoriteCourses(UUID userId, Pageable pageable) {
        return PageResponse.ofSlice(
                courseRepository.findFavoritedByOthers(userId, pageable),
                courseEntity -> CourseSummaryResponse.from(courseEntity, userId, true)
        );
    }

    // 탐색탭 — 공개 코스 (Slice). userId 있으면 페이지 단위 favorite 여부 batch 조회.
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getPublicList(UUID userId, String keyword, Pageable pageable) {
        var slice = (keyword == null || keyword.isBlank())
                ? courseRepository.findByIsPublicTrue(pageable)
                : courseRepository.searchPublicByName(keyword, pageable);

        // 페이지에 포함된 courseId만 대상으로 favorite 여부 batch 조회 (N+1 방지)
        List<UUID> pageIds = slice.getContent().stream().map(CourseEntity::getId).toList();
        Set<UUID> favoritedIds = (userId == null || pageIds.isEmpty())
                ? Set.of()
                : Set.copyOf(courseRepository.findFavoritedCourseIdsIn(userId, pageIds));

        return PageResponse.ofSlice(
                slice,
                courseEntity -> CourseSummaryResponse.from(courseEntity, userId, favoritedIds.contains(courseEntity.getId()))
        );
    }

    // 상세 조회 — 비소유자 접근 시 view_count +1 (원자적 UPDATE)
    // 카운트를 증가시키므로 readOnly 아님
    @Transactional
    public CourseDetailResponse getDetail(UUID courseId, UUID userId) {
        CourseEntity courseEntity = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));

        validateDetailAccess(courseEntity, userId);

        // 비소유자 조회만 view count 반영. 소유자 자신의 조회는 무시(수치 오염 방지).
        if (!courseEntity.isOwner(userId)) {
            courseRepository.incrementViewCount(courseId);
            // 응답에도 반영되도록 로컬 필드 수동 +1 (재조회 회피)
            courseEntity = courseRepository.findByIdWithUser(courseId)
                    .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));
        }

        List<CourseWaypointResponse> waypoints = courseWaypointRepository
                .findByCourseEntityIdWithPlaceOrderBySeqAsc(courseId)
                .stream().map(CourseWaypointResponse::from).toList();

        boolean isFavorited = !courseEntity.isOwner(userId)
                && courseFavoriteRepository.existsById(new CourseFavoriteId(courseId, userId));
        return CourseDetailResponse.from(courseEntity, waypoints, userId, isFavorited);
    }

    @Transactional
    public boolean addFavorite(UUID courseId, UUID userId) {
        CourseEntity courseEntity = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));

        if (courseEntity.isOwner(userId)) throw new BusinessException(COURSE_FAVORITE_OWN_COURSE);
        if (!courseEntity.isPublic())     throw new BusinessException(COURSE_ACCESS_DENIED);

        CourseFavoriteId favId = new CourseFavoriteId(courseId, userId);
        if (courseFavoriteRepository.existsById(favId)) {
            throw new BusinessException(COURSE_FAVORITE_ALREADY_EXISTS);
        }

        courseFavoriteRepository.save(CourseFavoriteEntity.create(courseId, userId));
        courseRepository.incrementLikeCount(courseId);
        return true;
    }

    @Transactional
    public boolean removeFavorite(UUID courseId, UUID userId) {
        CourseFavoriteId favoriteId = new CourseFavoriteId(courseId, userId);
        if (!courseFavoriteRepository.existsById(favoriteId)) {
            throw new BusinessException(COURSE_FAVORITE_NOT_FOUND);
        }
        courseFavoriteRepository.deleteById(favoriteId);
        courseRepository.decrementLikeCount(courseId);
        return false;
    }

    // 네이버 지도 길찾기 연동 카운트 +1 (추후 앱 딥링크 트리거 지점에서 호출)
    // 소유자·비소유자 구분 없이 카운트 (실제 라이딩 시도 지표라 자기 코스도 포함이 자연스러움)
    @Transactional
    public void incrementNavigateCount(UUID courseId, UUID userId) {
        CourseEntity courseEntity = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));
        validateDetailAccess(courseEntity, userId);
        courseRepository.incrementNavigateCount(courseId);
    }

    // 코스 hard delete — 작성자 본인만 가능
    // CASCADE로 waypoints, favorites 자동 삭제
    // source_course_id ON DELETE SET NULL로 파생 코스는 유지 (source 참조만 NULL로)
    @Transactional
    public void deleteCourse(UUID courseId, UUID userId) {
        CourseEntity courseEntity = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));
        if (!courseEntity.isOwner(userId)) {
            throw new BusinessException(COURSE_ACCESS_DENIED);
        }
        courseRepository.delete(courseEntity);
    }

    private void validateDetailAccess(CourseEntity course, UUID userId) {
        if (course.isOwner(userId)) return;
        if (course.isPublic()) return;
        boolean favoritedByMe = courseFavoriteRepository
                .existsById(new CourseFavoriteId(course.getId(), userId));
        if (favoritedByMe) return;
        throw new BusinessException(COURSE_ACCESS_DENIED);
    }

    @Transactional
    public CourseDetailResponse createCourse(UUID userId, CourseCreateRequest request) {
        List<WaypointRequest> waypoints = request.waypoints();
        validateWaypoints(waypoints);
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        CourseEntity saved = courseRepository.save(
                CourseEntity.createWithId(
                    UUID.randomUUID(),
                    user,
                    request.name(),
                    request.description(),
                    request.distanceMeters(),
                    request.path(),
                    request.bbox(),
                    request.isPublic(),
                    request.sourceCourseId())
        );
        saveWaypoints(saved, waypoints);

        // 복사 편집 — 원본 코스의 copy_count +1 (원본이 남아있을 때만)
        if (request.sourceCourseId() != null) {
            courseRepository.findById(request.sourceCourseId())
                    .ifPresent(src -> courseRepository.incrementCopyCount(src.getId()));
        }

        return buildDetailResponse(saved.getId(), userId);

    }

    @Transactional
    public CourseDetailResponse updateCourse(UUID courseId, UUID userId, CourseUpdateRequest request) {
        CourseEntity courseEntity = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));

        if (!courseEntity.isOwner(userId)) {
            throw new BusinessException(COURSE_ACCESS_DENIED);
        }

        boolean isPublic = request.isPublic() != null ? request.isPublic() : courseEntity.isPublic();
        courseEntity.update(request.name(), request.description(), isPublic);

        List<WaypointRequest> waypoints = request.waypoints();
        if (request.regeneratePath() && waypoints != null && !waypoints.isEmpty()) {
            validateWaypoints(waypoints);

            String path = request.path();
            if (path == null || path.isBlank() || request.distanceMeters() == null) {
                throw new BusinessException(COURSE_INVALID_WAYPOINTS);
            }

            sanityCheckPath(path, request.distanceMeters(), waypoints);
            courseEntity.updatePath(path, request.distanceMeters(), request.bbox());

            // UNIQUE (course_id, seq) 충돌 방지 (delete -> insert)
            courseWaypointRepository.deleteByCourseEntityId(courseId);
            saveWaypoints(courseEntity, waypoints);
        }

        return buildDetailResponse(courseId, userId);
    }

    // preview는 Naver 호출만, DB 접근 없음 — 트랜잭션 어노테이션 생략
    public CoursePreviewResponse previewCourse(CoursePreviewRequest request) {
        List<WaypointRequest> waypoints = request.waypoints();
        validateWaypoints(waypoints);
        DirectionsResult result = callNaverDirections(waypoints);
        return new CoursePreviewResponse(result.pathJson, result.distanceMeters, result.bbox);
    }

    private void validateWaypoints(List<WaypointRequest> waypoints) {
        waypoints.stream()
                .filter(w -> !WAYPOINT_ROLE.contains(w.role()))
                .findAny()
                .ifPresent(w -> {
                    throw new BusinessException(INVALID_INPUT);
                });

        long startCount = waypoints.stream().filter(w -> "START".equals(w.role())).count();
        long endCount   = waypoints.stream().filter(w -> "GOAL".equals(w.role())).count();
        long viaCount   = waypoints.stream().filter(w -> "VIA".equals(w.role())).count();

        if (startCount != 1 || endCount != 1) throw new BusinessException(COURSE_INVALID_WAYPOINTS);
        // 네이버 지도 앱 자동차 길찾기 URL scheme이 경유지 최대 5개 (v1~v5)라
        // 서버도 Directions 5만 사용. 사용자가 저장한 코스를 앱 딥링크로 열 때 잘리지 않도록.
        if (viaCount > 5) throw new BusinessException(COURSE_DIRECTIONS_WAYPOINTS_LIMIT);

        long count = waypoints.stream().mapToInt(WaypointRequest::seq).distinct().count();
        if(count != waypoints.size()) throw new BusinessException(COURSE_INVALID_WAYPOINTS);
    }

    private DirectionsResult callNaverDirections(List<WaypointRequest> waypoints) {
        WaypointRequest start = waypoints.stream().filter(w -> "START".equals(w.role())).findFirst().orElseThrow();
        WaypointRequest goal = waypoints.stream().filter(w -> "GOAL".equals(w.role())).findFirst().orElseThrow();
        List<Waypoint> vias = waypoints.stream()
                .filter(w -> "VIA".equals(w.role()))
                .sorted(Comparator.comparingInt(WaypointRequest::seq))
                .map(w -> new Waypoint(w.latitude(), w.longitude()))
                .toList();

        NaverDirectionsResponse response = naverMapsClient.directions(
                start.longitude(), start.latitude(),
                goal.longitude(), goal.latitude(),
                vias);
        RoutePath routePath = response.route().traavoidcaronly().get(0);
        String pathJson;
        try {
            pathJson = objectMapper.writeValueAsString(routePath.path());
        } catch (JsonProcessingException e) {
            throw new BusinessException(COURSE_DIRECTIONS_FAILED);
        }

        int distanceMeters = routePath.summary().distance();
        List<List<Double>> bbox = routePath.summary().bbox();
        return new DirectionsResult(pathJson, distanceMeters, bbox);
    }
    private void saveWaypoints(CourseEntity courseEntity, List<WaypointRequest> waypoints) {
        for (WaypointRequest waypoint : waypoints) {
            CourseWaypointEntity waypointEntity;

            UUID placeId = waypoint.placeId();
            if (placeId != null) {
                PlaceEntity placeEntity = placeRepository.findById(placeId)
                        .orElseThrow(() -> new BusinessException(PLACE_NOT_FOUND));
                waypointEntity = CourseWaypointEntity.createWithPlace(courseEntity, placeEntity, waypoint.seq(), waypoint.role());
            } else {
                waypointEntity = CourseWaypointEntity.create(courseEntity, waypoint.seq(), waypoint.role(), waypoint.placeName(),
                        waypoint.latitude(), waypoint.longitude());
            }
            courseWaypointRepository.save(waypointEntity);
        }
    }

    private CourseDetailResponse buildDetailResponse(UUID courseId, UUID userId) {
        CourseEntity courseEntity = courseRepository.findByIdWithUser(courseId).orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));

        List<CourseWaypointResponse> waypointResponses = courseWaypointRepository.findByCourseEntityIdWithPlaceOrderBySeqAsc(courseId)
                .stream().map(CourseWaypointResponse::from).toList();
        boolean isFavorited = !courseEntity.isOwner(userId) && courseFavoriteRepository.existsById(new CourseFavoriteId(courseId, userId));
        return CourseDetailResponse.from(courseEntity, waypointResponses, userId, isFavorited);
    }

    // 옵션 B sanity check: 앱이 전송한 path/distance가 waypoints와 대략 일치하는지 최소 검증
// 사용자 본인 코스라 조작 인센티브 낮음 → 파싱 가능 + start/goal 좌표 근사 일치만 확인
    private void sanityCheckPath(String pathJson, int distanceMeters, List<WaypointRequest> waypoints) {
        if (distanceMeters <= 0) {
            throw new BusinessException(COURSE_INVALID_WAYPOINTS);
        }
        List<List<Double>> path;
        try {
            path = objectMapper.readValue(pathJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException(COURSE_INVALID_WAYPOINTS);
        }
        if (path.isEmpty() || path.get(0).size() < 2 || path.get(path.size() - 1).size() < 2) {
            throw new BusinessException(COURSE_INVALID_WAYPOINTS);
        }

        WaypointRequest start = waypoints.stream()
                .filter(w -> "START".equals(w.role())).findFirst().orElseThrow();
        WaypointRequest goal = waypoints.stream()
                .filter(w -> "GOAL".equals(w.role())).findFirst().orElseThrow();

        // path[0] = [lng, lat], path[last] = [lng, lat]
        double pathStartLng = path.get(0).get(0);
        double pathStartLat = path.get(0).get(1);
        double pathGoalLng  = path.get(path.size() - 1).get(0);
        double pathGoalLat  = path.get(path.size() - 1).get(1);

        // 0.001도 ≈ 100m 이내 오차 허용 (Directions는 인접 도로로 스냅되므로 정확 일치 안 함)
        if (Math.abs(pathStartLng - start.longitude().doubleValue()) > 0.001
                || Math.abs(pathStartLat - start.latitude().doubleValue()) > 0.001
                || Math.abs(pathGoalLng - goal.longitude().doubleValue()) > 0.001
                || Math.abs(pathGoalLat - goal.latitude().doubleValue()) > 0.001) {
            throw new BusinessException(COURSE_INVALID_WAYPOINTS);
        }
    }

    // Directions 결과 내부 record (bbox는 앱 지도 fitBounds용, DB 저장 안 함)
    private record DirectionsResult(String pathJson, int distanceMeters, List<List<Double>> bbox) {}
}
