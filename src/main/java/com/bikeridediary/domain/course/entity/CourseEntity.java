package com.bikeridediary.domain.course.entity;

import com.bikeridediary.domain.common.entity.BaseEntity;
import com.bikeridediary.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_course_user")
    )
    @OnDelete(action = OnDeleteAction.SET_NULL)
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

    // 경로 바운딩 박스 (JSON 문자열 — [[minLng,minLat],[maxLng,maxLat]])
    // 지도 fitBounds 용도. 저장 시 preview 응답 그대로 전송.
    @Column(name = "bbox", columnDefinition = "TEXT")
    private String bbox;

    // 공개 여부 (true=탐색탭 노출, false=작성자만 조회 가능)
    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(name = "source_course_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID sourceCourseId;

    // FK 정의 + @OnDelete 반영 담당. 쓰기는 위의 sourceCourseId 사용.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "source_course_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_course_source")
    )
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private CourseEntity sourceCourse;

    // 코스 설명
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 조회수 (비소유자가 상세를 열 때 +1)
    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    // 즐겨찾기 수 (favorite 추가 시 +1, 제거 시 -1)
    @Column(name = "like_count", nullable = false)
    private long likeCount = 0;

    // 복사 수 (다른 유저가 이 코스를 source로 새 코스를 만든 횟수)
    @Column(name = "copy_count", nullable = false)
    private long copyCount = 0;

    // 네이버 지도 길찾기 연동 횟수 (추후 기능)
    @Column(name = "navigate_count", nullable = false)
    private long navigateCount = 0;

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

    // Directions API 재호출 결과로 path/distance/bbox 업데이트 (dirty checking)
    public void updatePath(String path, Integer distanceMeters, String bbox) {
        this.path = path;
        this.distanceMeters = distanceMeters;
        this.bbox = bbox;
    }


    // 코스 생성 팩토리
    public static CourseEntity createWithId(
        UUID id,
        UserEntity userEntity,
        String name,
        String description,
        Integer distanceMeters,
        String path,
        String bbox,
        boolean isPublic,
        UUID sourceCourseId
    ) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.id = id;
        courseEntity.userEntity = userEntity;
        courseEntity.name = name;
        courseEntity.distanceMeters = distanceMeters;
        courseEntity.path = path;
        courseEntity.bbox = bbox;
        courseEntity.isPublic = isPublic;
        courseEntity.sourceCourseId = sourceCourseId;
        courseEntity.description = description;
        return courseEntity;
    }
}
