package com.bikeridediary.domain.place.entity;

import com.bikeridediary.domain.course.entity.CourseFavoriteEntity;
import com.bikeridediary.domain.course.entity.CourseFavoriteId;
import com.bikeridediary.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "place_wishes",
        indexes = {
                @Index(name = "idx_place_wishes_user", columnList = "user_id"),
                @Index(name = "idx_place_wishes_user_id", columnList = "place_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceWishEntity {


    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval)
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    @EmbeddedId
    private PlaceWishId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", insertable = false, updatable = false)
    private PlaceEntity placeEntity;

    @CreatedDate
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static PlaceWishEntity create(UUID placeId, UUID userId) {
        PlaceWishEntity e = new PlaceWishEntity();
        e.id = new PlaceWishId(placeId, userId);
        return e;
    }
}
