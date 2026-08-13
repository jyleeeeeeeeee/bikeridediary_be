package com.bikeridediary.infra.opinet;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "areas")
@Data
public class AreaEntity {

    @EmbeddedId
    private AreaId id;

    /**
     * 복합 키(PK) 정의 클래스
     */
    @Embeddable
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class AreaId implements Serializable {

        @Column(name = "area_cd", nullable = false, length = 5)
        private String areaCd; // 지역코드

        @Column(name = "area_nm", nullable = false, length = 10)
        private String areaNm; // 지역명
    }
}
