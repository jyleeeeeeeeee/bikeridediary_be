package com.bikeridediary.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 모든 엔티티가 상속받는 기본 추상 클래스 - 공통 감시 필드(created_at, updated_at, deleted_at) 제공
@MappedSuperclass
@Getter
public abstract class BaseEntity {

    // 등록 일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 일시
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 삭제 일시 (소프트 삭제)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 엔티티를 소프트 삭제 처리 (실제 삭제하지 않고 deleted_at만 설정)
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    // 엔티티가 삭제되었는지 확인
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
