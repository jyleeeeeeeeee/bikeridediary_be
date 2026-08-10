package com.bikeridediary.domain.course.entity;

import com.bikeridediary.domain.common.entity.BaseEntity;
import com.bikeridediary.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Entity
@Table(name = "courses")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseEntity extends BaseEntity {

    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval)
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    // 코스 ID (클라이언트 UUID)
    @Id
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    // 코스 작성자 (FK, nullable — 시드/큐레이션 코스는 작성자 없음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private UserEntity userEntity;

    // 코스 이름
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    // 총 거리 (미터 단위)
    @Column(name = "distance_meters")
    private Integer distanceMeters;

    // 경로 좌표 배열 (JSON 문자열 — [[lng,lat],[lng,lat]...], TEXT 컬럼)
    @Column(name = "path", columnDefinition = "TEXT")
    private String path;

    // 공개 여부 (true=탐색탭 노출, false=작성자만 조회 가능)
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    // 원본 코스 ID (남의 코스를 복제해서 저장할 때 참조, 자기참조 FK)
    @Column(name = "source_course_id")
    private UUID sourceCourseId;

    // 코스 설명
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 이 코스가 특정 사용자에게 속하는지 확인 (권한 검증용)
    // 시드 코스(userEntity=null)는 그 누구의 소유도 아님
    public boolean isOwner(UUID userId) {
        if (this.userEntity == null || userId == null) return false;
        return this.userEntity.getId().equals(userId);
    }

    // 코스 기본 정보 업데이트 (JPA dirty checking — save() 호출 불필요)
    public void update(String name, String description, boolean isPublic) {
        if (name != null) this.name = name;
        // description: null이면 변경 없음, 빈 문자열이면 null로 저장(제거)
        if (description != null) {
            this.description = description.isBlank() ? null : description;
        }
        this.isPublic = isPublic;
    }

    // Directions API 재호출 결과로 path/distance 업데이트 (dirty checking)
    public void updatePath(String path, Integer distanceMeters) {
        this.path = path;
        this.distanceMeters = distanceMeters;
    }


    // 코스 생성 팩토리
    public static CourseEntity createWithId(
        UUID id,
        UserEntity userEntity,
        String name,
        String description,
        Integer distanceMeters,
        String path,
        boolean isPublic,
        UUID sourceCourseId
    ) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.id = id;
        courseEntity.userEntity = userEntity;
        courseEntity.name = name;
        courseEntity.distanceMeters = distanceMeters;
        courseEntity.path = path;
        courseEntity.isPublic = isPublic;
        courseEntity.sourceCourseId = sourceCourseId;
        courseEntity.description = description;
        return courseEntity;
    }
}
