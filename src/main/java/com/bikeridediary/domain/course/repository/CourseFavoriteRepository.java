package com.bikeridediary.domain.course.repository;

import com.bikeridediary.domain.course.entity.CourseFavoriteEntity;
import com.bikeridediary.domain.course.entity.CourseFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseFavoriteRepository extends JpaRepository<CourseFavoriteEntity, CourseFavoriteId> {
}
