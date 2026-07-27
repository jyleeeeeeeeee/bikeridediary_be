package com.bikeridediary.domain.course.service;

import com.bikeridediary.domain.course.dto.CourseDetailResponse;
import com.bikeridediary.domain.course.dto.CourseSummaryResponse;
import com.bikeridediary.domain.course.dto.CourseWaypointResponse;
import com.bikeridediary.domain.course.entity.CourseEntity;
import com.bikeridediary.domain.course.entity.CourseFavoriteEntity;
import com.bikeridediary.domain.course.entity.CourseFavoriteId;
import com.bikeridediary.domain.course.repository.CourseFavoriteRepository;
import com.bikeridediary.domain.course.repository.CourseRepository;
import com.bikeridediary.domain.course.repository.CourseWaypointRepository;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.bikeridediary.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseFavoriteRepository courseFavoriteRepository;
    private final CourseWaypointRepository courseWaypointRepository;

    // 내가 만든 코스 (페이징) — 항상 isMineOnly=true 로 매핑되므로 isFavorited=false
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getMyOwnedCourses(UUID userId, Pageable pageable) {
        return PageResponse.of(
                courseRepository.findByUserEntityId(userId, pageable),
                course -> CourseSummaryResponse.from(course, userId, false)
        );
    }

    // 내가 즐겨찾기한 코스 (Slice) — 무한 스크롤용, count 절약
    @Transactional(readOnly = true)
    public PageResponse<CourseSummaryResponse> getMyFavoriteCourses(UUID userId, Pageable pageable) {
        return PageResponse.ofSlice(
                courseRepository.findFavoritedByOthers(userId, pageable),
                course -> CourseSummaryResponse.from(course, userId, true)
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
                course -> CourseSummaryResponse.from(course, userId, favoritedIds.contains(course.getId()))
        );
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getDetail(UUID courseId, UUID userId) {
        CourseEntity course = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));

        validateDetailAccess(course, userId);

        List<CourseWaypointResponse> waypoints = courseWaypointRepository
                .findByCourseEntityIdWithPlaceOrderBySeqAsc(courseId)
                .stream().map(CourseWaypointResponse::from).toList();

        boolean isFavorited = !course.isOwner(userId)
                && courseFavoriteRepository.existsById(new CourseFavoriteId(courseId, userId));
        return CourseDetailResponse.from(course, waypoints, userId, isFavorited);
    }

    @Transactional
    public boolean addFavorite(UUID courseId, UUID userId) {
        CourseEntity course = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));

        if (course.isOwner(userId)) throw new BusinessException(COURSE_FAVORITE_OWN_COURSE);
        if (!course.isPublic())     throw new BusinessException(COURSE_ACCESS_DENIED);

        CourseFavoriteId favId = new CourseFavoriteId(courseId, userId);
        if (courseFavoriteRepository.existsById(favId)) {
            throw new BusinessException(COURSE_FAVORITE_ALREADY_EXISTS);
        }

        courseFavoriteRepository.save(CourseFavoriteEntity.create(courseId, userId));
        return true;
    }

    @Transactional
    public boolean removeFavorite(UUID courseId, UUID userId) {
        CourseFavoriteId favId = new CourseFavoriteId(courseId, userId);
        if (!courseFavoriteRepository.existsById(favId)) {
            throw new BusinessException(COURSE_FAVORITE_NOT_FOUND);
        }
        courseFavoriteRepository.deleteById(favId);
        return false;
    }

    // 코스 hard delete — 작성자 본인만 가능
    // CASCADE로 waypoints, favorites 자동 삭제
    // source_course_id ON DELETE SET NULL로 파생 코스는 유지 (source 참조만 NULL로)
    public void deleteCourse(UUID courseId, UUID userId) {
        CourseEntity course = courseRepository.findByIdWithUser(courseId)
                .orElseThrow(() -> new BusinessException(COURSE_NOT_FOUND));
        if (!course.isOwner(userId)) {
            throw new BusinessException(COURSE_ACCESS_DENIED);
        }
        courseRepository.delete(course);
    }

    private void validateDetailAccess(CourseEntity course, UUID userId) {
        if (course.isOwner(userId)) return;
        if (course.isPublic()) return;
        boolean favoritedByMe = courseFavoriteRepository
                .existsById(new CourseFavoriteId(course.getId(), userId));
        if (favoritedByMe) return;
        throw new BusinessException(COURSE_ACCESS_DENIED);
    }

}
