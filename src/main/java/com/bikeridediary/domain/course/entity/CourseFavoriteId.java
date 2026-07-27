package com.bikeridediary.domain.course.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

// 즐겨찾기 복합 PK — (course_id, user_id)
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CourseFavoriteId implements Serializable {

    @Column(name = "course_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID courseId;

    @Column(name = "user_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID userId;
}
