package com.bikeridediary.domain.course.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

// 즐겨찾기 복합 PK — (course_id, user_id)
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CourseFavoriteId implements Serializable {
    private UUID courseId;
    private UUID userId;
}