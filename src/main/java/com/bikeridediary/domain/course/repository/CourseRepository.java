package com.bikeridediary.domain.course.repository;

import com.bikeridediary.domain.course.entity.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    // MY탭 — 내가 만든 코스 (Page: 유저 본인 콘텐츠 총 개수는 유용)
    Page<CourseEntity> findByUserEntityId(UUID userId, Pageable pageable);

    // MY탭 — 내가 즐겨찾기한 코스 (Slice: 무한 스크롤용, count 오버헤드 회피)
    // 즐겨찾기 등록 후 작성자가 비공개로 전환해도 목록 유지
    @Query("""
                SELECT c FROM CourseEntity c
                JOIN CourseFavoriteEntity f ON f.id.courseId = c.id
                WHERE f.id.userId = :userId
                    AND (c.userEntity IS NULL OR c.userEntity.id <> :userId)
            """)
    Slice<CourseEntity> findFavoritedByOthers(@Param("userId") UUID userId, Pageable pageable);

    // 공개 리스트 favorite 플래그 계산용 — 페이지 대상 courseId만 조회
    @Query("""
            SELECT f.id.courseId FROM CourseFavoriteEntity f
            WHERE f.id.userId = :userId
              AND f.id.courseId IN :courseIds
            """)
    List<UUID> findFavoritedCourseIdsIn(
            @Param("userId") UUID userId,
            @Param("courseIds") List<UUID> courseIds);

    // 탐색탭 — 공개 코스 전체 (Slice: 총 개수 count 무거워질 것 대비, 무한 스크롤만 필요)
    // 정렬은 Pageable Sort로 지정 (Controller의 @PageableDefault로 createdAt DESC 기본)
    Slice<CourseEntity> findByIsPublicTrue(Pageable pageable);

    // 탐색탭 — 코스명 부분일치 검색 (Slice: 무한 스크롤)
    @Query("""
            SELECT c FROM CourseEntity c
            WHERE c.isPublic = TRUE
              AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Slice<CourseEntity> searchPublicByName(@Param("keyword") String keyword, Pageable pageable);

    // 상세 조회 — User fetch join (waypoints는 별도 Repository로 분리 조회)
    @Query("""
            SELECT DISTINCT c FROM CourseEntity c
            LEFT JOIN FETCH c.userEntity
            WHERE c.id = :courseId
            """)
    Optional<CourseEntity> findByIdWithUser(@Param("courseId") UUID courseId);

    // no(자동 증가 조회 번호)로 특정 course 조회 — DB 관리/디버깅용
    Optional<CourseEntity> findByNo(Long no);

    // ── 카운트 원자적 증감 (UPDATE ... SET x = x ± 1) ─────────────────────────
    // dirty checking 대신 @Query 사용해 race condition 방어 + 1회 UPDATE로 처리
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CourseEntity c SET c.viewCount = c.viewCount + 1 WHERE c.id = :courseId")
    void incrementViewCount(@Param("courseId") UUID courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CourseEntity c SET c.likeCount = c.likeCount + 1 WHERE c.id = :courseId")
    void incrementLikeCount(@Param("courseId") UUID courseId);

    // 0 미만으로 내려가지 않도록 GREATEST 계열 대신 WHERE 조건으로 방어 (Postgres 안전)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CourseEntity c SET c.likeCount = c.likeCount - 1 WHERE c.id = :courseId AND c.likeCount > 0")
    void decrementLikeCount(@Param("courseId") UUID courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CourseEntity c SET c.copyCount = c.copyCount + 1 WHERE c.id = :courseId")
    void incrementCopyCount(@Param("courseId") UUID courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE CourseEntity c SET c.navigateCount = c.navigateCount + 1 WHERE c.id = :courseId")
    void incrementNavigateCount(@Param("courseId") UUID courseId);
}
