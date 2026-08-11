package com.bikeridediary.domain.course.repository;

import com.bikeridediary.domain.course.entity.CourseWaypointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseWaypointRepository extends JpaRepository<CourseWaypointEntity, UUID> {
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

    // update 시 기존 waypoint 모두 삭제.
    // @Modifying + flushAutomatically=true 로 즉시 실행 (파생 delete는 flush 순서에서
    // INSERT보다 뒤로 밀려 UNIQUE (course_id, seq) 충돌 발생하므로 벌크 DELETE로 대체).
    // clearAutomatically=true 로 컨텍스트 정리 → 이후 INSERT가 stale 상태 참조 안 함.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CourseWaypointEntity w WHERE w.courseEntity.id = :courseId")
    void deleteByCourseEntityId(@Param("courseId") UUID courseId);
}
