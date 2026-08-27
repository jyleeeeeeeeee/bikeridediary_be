package com.bikeridediary.domain.maintenance.entity;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.common.entity.BaseEntity;
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

import java.time.LocalDate;
import java.util.UUID;

// 정비 주기 관리 엔티티 - km 또는 개월 기준 정비 주기를 관리
// 마지막 정비 정보(주행거리/날짜)는 정비 기록(MaintenanceEntity)에서 조회
@Entity
@Table(name = "maintenance_schedules")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceScheduleEntity extends BaseEntity {

    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval)
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    // 정비 주기 ID (UUID) — 클라이언트가 생성한 UUID 그대로 수용 (로컬 우선 sync)
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "bike_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedule_bike")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BikeEntity bike;

    // 정비 종류 (엔진오일, 체인, 타이어, 브레이크 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MaintenanceType maintenanceType;

    // km 기준 정비 주기 (nullable - 없으면 km 기준 정비 안 함)
    @Column(name = "interval_km")
    private Long intervalKm;

    // 개월 기준 정비 주기 (nullable - 없으면 날짜 기준 정비 안 함)
    @Column(name = "interval_months")
    private Integer intervalMonths;

    // 정비 주기 엔티티 생성 (서버에서 UUID 생성)
    public static MaintenanceScheduleEntity create(
            BikeEntity bikeEntity,
            MaintenanceType maintenanceType,
            Long intervalKm,
            Integer intervalMonths
    ) {
        return createWithId(UUID.randomUUID(), bikeEntity, maintenanceType, intervalKm, intervalMonths);
    }

    // 정비 주기 엔티티 생성 (클라이언트 UUID 그대로 수용) — sync 전용
    public static MaintenanceScheduleEntity createWithId(
            UUID id,
            BikeEntity bikeEntity,
            MaintenanceType maintenanceType,
            Long intervalKm,
            Integer intervalMonths
    ) {
        MaintenanceScheduleEntity entity = new MaintenanceScheduleEntity();
        entity.id = id;
        entity.bike = bikeEntity;
        entity.maintenanceType = maintenanceType;
        entity.intervalKm = intervalKm;
        entity.intervalMonths = intervalMonths;
        return entity;
    }

    // 정비 주기 정보 수정
    public void update(Long intervalKm, Integer intervalMonths) {
        this.intervalKm = intervalKm;
        this.intervalMonths = intervalMonths;
    }

    // km 기준으로 정비 필요 여부 확인 (마지막 정비 주행거리는 정비 기록에서 전달)
    public boolean isOverdueByKm(Long currentMileage, Long lastMaintenanceMileage) {
        if (intervalKm == null || lastMaintenanceMileage == null) {
            return false;
        }
        return currentMileage > (lastMaintenanceMileage + intervalKm);
    }

    // 날짜 기준으로 정비 필요 여부 확인 (마지막 정비 날짜는 정비 기록에서 전달)
    public boolean isOverdueByDate(LocalDate currentDate, LocalDate lastMaintenanceDate) {
        if (intervalMonths == null || lastMaintenanceDate == null) {
            return false;
        }
        return currentDate.isAfter(lastMaintenanceDate.plusMonths(intervalMonths));
    }

    // km 또는 날짜 중 하나라도 정비 주기를 초과했으면 true 반환
    public boolean isOverdue(Long currentMileage, LocalDate currentDate,
                             Long lastMaintenanceMileage, LocalDate lastMaintenanceDate) {
        return isOverdueByKm(currentMileage, lastMaintenanceMileage)
                || isOverdueByDate(currentDate, lastMaintenanceDate);
    }

    // 이 정비 주기가 특정 사용자의 바이크에 속하는지 권한 검증
    public boolean isOwner(UUID userId) {
        return this.bike.isOwner(userId);
    }

}
