package com.bikeridediary.domain.course.repository;

import com.bikeridediary.domain.course.entity.CourseWaypointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseWaypointRepository extends JpaRepository<CourseWaypointEntity, UUID> {
    List<CourseWaypointEntity> findByCourseEntityIdOrderBySeqAsc(UUID courseId);
    @Query("""
        SELECT w FROM CourseWaypointEntity w
        LEFT JOIN FETCH w.placeEntity p
        LEFT JOIN FETCH p.placeCategoryEntity
        WHERE w.courseEntity.id = :courseId
        ORDER BY w.seq ASC
        """)
    List<CourseWaypointEntity> findByCourseEntityIdWithPlaceOrderBySeqAsc(@Param("courseId") UUID courseId);

    // no(자동 증가 조회 번호)로 특정 waypoint 조회 — DB 관리/디버깅용
    Optional<CourseWaypointEntity> findByNo(Long no);
}
