package com.bikeridediary.domain.course.repository;

import com.bikeridediary.domain.course.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    // MY탭 — 내가 만든 코스 (hard delete 정책이라 deleted_at 필터 없음)
    List<CourseEntity> findByUserEntityIdOrderByCreatedAtDesc(UUID userId);

    // MY탭 — 내가 즐겨찾기한 코스 (남의 코스 + 시드 코스 모두 포함, 내 것만 제외)
    // 주의: isPublic 조건 제거 — 즐겨찾기 등록 후 작성자가 비공개로 전환해도 목록 유지
    //       (상세 접근도 validateDetailAccess에서 즐겨찾기 케이스 허용)
    @Query("""
                SELECT c FROM CourseEntity c
                JOIN CourseFavoriteEntity f ON f.id.courseId = c.id
                WHERE f.id.userId = :userId
                    AND (c.userEntity IS NULL OR c.userEntity.id <> :userId)
                ORDER BY f.createdAt DESC
            """)
    List<CourseEntity> findFavoritedByOthers(@Param("userId") UUID userId);

    // 탐색탭 — 공개 코스 전체
    List<CourseEntity> findByIsPublicTrueOrderByCreatedAtDesc();

    // 탐색탭 — 코스명 부분일치 검색
    @Query("""
            SELECT c FROM CourseEntity c
            WHERE c.isPublic = TRUE
              AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY c.createdAt DESC
            """)
    List<CourseEntity> searchPublicByName(@Param("keyword") String keyword);

    // 상세 조회 — User fetch join (waypoints는 별도 Repository로 분리 조회)
    @Query("""
            SELECT DISTINCT c FROM CourseEntity c
            LEFT JOIN FETCH c.userEntity
            WHERE c.id = :courseId
            """)
    Optional<CourseEntity> findByIdWithUser(@Param("courseId") UUID courseId);

}
