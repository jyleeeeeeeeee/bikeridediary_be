package com.bikeridediary.domain.user.repository;

import com.bikeridediary.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    // OAuth2 제공자 + 제공자 ID로 조회 (로그인 시 사용)
    Optional<UserEntity> findByProviderAndProviderId(String provider, String providerId);

    // 활성 사용자 (삭제되지 않은) ID로 조회
    Optional<UserEntity> findByIdAndDeletedAtIsNull(UUID id);

    // 활성 사용자 이메일로 조회 (일반 로그인)
    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);

    // 활성 사용자 중 이메일 중복 확인
    boolean existsByEmailAndDeletedAtIsNull(String email);

}
