package com.bikeridediary.domain.bikemodel.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 바이크 모델 마스터 데이터 — API-Ninjas로부터 동기화
@Entity
@Table(name = "bike_models",
       uniqueConstraints = @UniqueConstraint(columnNames = {"manufacturer_id", "name", "year"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BikeModelEntity {

    // 모델 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 제조사 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private ManufacturerEntity manufacturer;

    // 모델명 (예: "CBR650R", "Monster 821")
    @Column(nullable = false, length = 150)
    private String name;

    // 연식
    @Column
    private Integer year;

    // 차종 (Sport, Naked, Touring 등 — API 원본 값)
    @Column(length = 50)
    private String type;

    // 배기량 (예: "649.0 ccm (39.60 cubic inches)")
    @Column(length = 100)
    private String displacement;

    // 엔진 (예: "In-line four, four-stroke")
    @Column(length = 200)
    private String engine;

    // 출력 (예: "93.9 HP (68.5 kW)) @ 12000 RPM")
    @Column(length = 150)
    private String power;

    // 토크 (예: "63.0 Nm @ 8500 RPM")
    @Column(length = 150)
    private String torque;

    // 건조 중량 (예: "208.0 kg (458.6 pounds)")
    @Column(name = "total_weight", length = 100)
    private String totalWeight;

    // 시트 높이 (예: "810 mm (31.9 inches)")
    @Column(name = "seat_height", length = 100)
    private String seatHeight;

    // 연료 탱크 용량 (예: "15.40 litres (4.07 US gallons)")
    @Column(name = "fuel_capacity", length = 100)
    private String fuelCapacity;

    // 등록 일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static BikeModelEntity create(
            ManufacturerEntity manufacturer,
            String name,
            Integer year,
            String type,
            String displacement,
            String engine,
            String power,
            String torque,
            String totalWeight,
            String seatHeight,
            String fuelCapacity
    ) {
        BikeModelEntity entity = new BikeModelEntity();
        entity.manufacturer = manufacturer;
        entity.name = name;
        entity.year = year;
        entity.type = type;
        entity.displacement = displacement;
        entity.engine = engine;
        entity.power = power;
        entity.torque = torque;
        entity.totalWeight = totalWeight;
        entity.seatHeight = seatHeight;
        entity.fuelCapacity = fuelCapacity;
        return entity;
    }
}
