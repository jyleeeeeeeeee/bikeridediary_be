package com.bikeridediary.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlaceWishId implements Serializable {
    @Column(name = "place_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID placeId;

    @Column(name = "user_id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID userId;
}