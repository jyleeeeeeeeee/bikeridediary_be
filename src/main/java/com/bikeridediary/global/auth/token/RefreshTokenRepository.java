package com.bikeridediary.global.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Refresh Token을 Redis에 저장/조회/삭제하는 저장소.
// Key: "refresh_token:{userId}", TTL: 30일
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 30;

    // Refresh Token을 Redis에 저장
    public void save(UUID userId, String refreshToken) {
        String key = PREFIX + userId;
        redisTemplate.opsForValue().set(
                key,
                refreshToken,
                REFRESH_TOKEN_EXPIRY_DAYS,
                TimeUnit.DAYS
        );
    }

    // userId로 저장된 Refresh Token 조회
    public Optional<String> findByUserId(UUID userId) {
        String key = PREFIX + userId;
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    // Refresh Token 존재 여부 확인
    public boolean exists(UUID userId) {
        String key = PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Refresh Token 삭제 (로그아웃 시)
    public void delete(UUID userId) {
        String key = PREFIX + userId;
        redisTemplate.delete(key);
    }

    // Refresh Token 유효성 검증. 저장된 토큰과 일치하면 true.
    public boolean isValid(UUID userId, String refreshToken) {
        return findByUserId(userId)
                .map(token -> token.equals(refreshToken))
                .orElse(false);
    }
}
