package com.bikeridediary.domain.place.entity;


import com.bikeridediary.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

// 장소 변경 요청 큐 (CREATE / UPDATE_COORDINATES / UPDATE_INFO)
// 어드민이 승인/거절 처리하며, 승인 시 places 테이블에 반영된다.
@Entity
@Table(name = "place_change_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceChangeRequestEntity {

    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval)
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    @Id
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private PlaceChangeRequestType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "target_place_id",
            foreignKey = @ForeignKey(name = "fk_change_req_place")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlaceEntity targetPlace;

    @ManyToOne(fetch = FetchType.LAZY)   // optional = false 제거
    @JoinColumn(
            name = "requester_id",
            nullable = true,   // false → true
            foreignKey = @ForeignKey(name = "fk_change_req_requester")
    )
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity requester;

    // type별 payload (JSONB)
    @Type(JsonType.class)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    // 상태 (PENDING / APPROVED / REJECTED)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlaceChangeRequestStatus status = PlaceChangeRequestStatus.PENDING;

    // 어드민 검토 노트
    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reviewed_by",
            foreignKey = @ForeignKey(name = "fk_change_req_reviewer")
    )
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity reviewer;

    // 검토 시각
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // 생성 시각 (수동 세팅. BaseEntity 안 씀 - updated_at/deleted_at 개념 없어서)
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 신규 요청 생성 팩토리
    public static PlaceChangeRequestEntity create(
            PlaceChangeRequestType type,
            PlaceEntity targetPlace,
            UserEntity requester,
            Map<String, Object> payload
    ) {
        PlaceChangeRequestEntity e = new PlaceChangeRequestEntity();
        e.id = UUID.randomUUID();
        e.type = type;
        e.targetPlace = targetPlace;
        e.requester = requester;
        e.payload = payload;
        e.status = PlaceChangeRequestStatus.PENDING;
        e.createdAt = LocalDateTime.now();
        return e;
    }

    // 어드민 승인 처리
    public void approve(UserEntity reviewer, String note) {
        this.status = PlaceChangeRequestStatus.APPROVED;
        this.reviewer = reviewer;
        this.reviewNote = note;
        this.reviewedAt = LocalDateTime.now();
    }

    // 어드민 거절 처리
    public void reject(UserEntity reviewer, String note) {
        this.status = PlaceChangeRequestStatus.REJECTED;
        this.reviewer = reviewer;
        this.reviewNote = note;
        this.reviewedAt = LocalDateTime.now();
    }
}
