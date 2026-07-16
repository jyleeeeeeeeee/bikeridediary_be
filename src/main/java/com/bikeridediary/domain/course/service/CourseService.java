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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.bikeridediary.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseFavoriteRepository courseFavoriteRepository;
    private final CourseWaypointRepository courseWaypointRepository;


    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getMyList(UUID userId) {
        List<CourseEntity> myCourses = courseRepository.findByUserEntityIdOrderByCreatedAtDesc(userId);
        List<CourseEntity> favorites = courseRepository.findFavoritedByOthers(userId);

        List<CourseSummaryResponse> result = new ArrayList<>();
        myCourses.forEach(course -> result.add(CourseSummaryResponse.from(course, userId, false)));
        favorites.forEach(course -> result.add(CourseSummaryResponse.from(course, userId, true)));
        return result;
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryResponse> getPublicList(UUID userId, String keyword) {
        List<CourseEntity> courses = (keyword == null || keyword.isBlank())
            ? courseRepository.findByIsPublicTrueOrderByCreatedAtDesc()
            : courseRepository.searchPublicByName(keyword);

        Set<UUID> myFavoriteIds = (userId == null) ? Set.of() : courseRepository.findFavoritedByOthers(userId)
                .stream().map(CourseEntity::getId).collect(Collectors.toSet());
        return courses.stream()
                .map(course -> CourseSummaryResponse.from(course, userId, myFavoriteIds.contains(course.getId())))
                .toList();
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
