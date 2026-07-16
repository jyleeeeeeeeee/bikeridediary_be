package com.bikeridediary.domain.course.entity;

import com.bikeridediary.domain.place.entity.PlaceEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "course_waypoints")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseWaypointEntity {

    // 코스 ID (클라이언트 UUID)
    @Id
    @Column(name = "id")
    private UUID id;

    // 소속 코스 (FK, 코스 삭제 시 CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity courseEntity;

    // 등록된 place 참조 (옵셔널) — 임의 지점은 null
    // ON DELETE SET NULL: place 삭제되어도 좌표 스냅샷으로 코스는 유효
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = true)
    private PlaceEntity placeEntity;

    // 순서 인덱스 (0-based, 정렬용)
    @Column(name = "seq", nullable = false)
    private short seq;

    // 역할 (START: 출발지, END: 목적지, VIA: 경유지)
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    // 지점 이름 스냅샷 (place 사용 시 등록 시점 place.placeName 복사, 임의 지점은 사용자 입력)
    @Column(name = "name", length = 200)
    private String name;

    // 위도 (소수점 7자리, 약 1.1cm 정밀도)
    @Column(name = "latitude", nullable = false, precision = 9, scale = 7)
    private BigDecimal latitude;

    // 경도 (소수점 7자리, 약 1.1cm 정밀도)
    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    // 임의 지점 waypoint 생성 (지도 롱프레스, GPX 임포트 등 place 없는 지점)
    public static CourseWaypointEntity create(
            CourseEntity courseEntity,
            short seq,
            String role,
            String name,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        CourseWaypointEntity e = new CourseWaypointEntity();
        e.id = UUID.randomUUID();
        e.courseEntity = courseEntity;
        e.placeEntity = null;
        e.seq = seq;
        e.role = role;
        e.name = name;
        e.latitude = latitude;
        e.longitude = longitude;
        return e;
    }

    // 등록된 place를 waypoint로 사용 시 생성 (좌표/이름은 place에서 스냅샷)
    public static CourseWaypointEntity createWithPlace(
            CourseEntity courseEntity,
            PlaceEntity placeEntity,
            short seq,
            String role
    ) {
        CourseWaypointEntity e = new CourseWaypointEntity();
        e.id = UUID.randomUUID();
        e.courseEntity = courseEntity;
        e.placeEntity = placeEntity;
        e.seq = seq;
        e.role = role;
        e.name = placeEntity.getPlaceName();
        e.latitude = placeEntity.getLatitude();
        e.longitude = placeEntity.getLongitude();
        return e;
    }
}
