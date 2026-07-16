package com.bikeridediary.domain.course.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

// 즐겨찾기 엔티티 — 공개된 남의 코스만 즐겨찾기 가능
@Entity
@Table(name = "course_favorites")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseFavoriteEntity {

    @EmbeddedId
    private CourseFavoriteId id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static CourseFavoriteEntity create(UUID courseId, UUID userId) {
        CourseFavoriteEntity e = new CourseFavoriteEntity();
        e.id = new CourseFavoriteId(courseId, userId);
        return e;
    }
}