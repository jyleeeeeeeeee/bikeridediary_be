package com.bikeridediary.domain.course.repository;

import com.bikeridediary.domain.course.entity.CourseFavoriteEntity;
import com.bikeridediary.domain.course.entity.CourseFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseFavoriteRepository extends JpaRepository<CourseFavoriteEntity, CourseFavoriteId> {

    // no(자동 증가 조회 번호)로 특정 favorite 조회 — DB 관리/디버깅용
    Optional<CourseFavoriteEntity> findByNo(Long no);
}
