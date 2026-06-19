package com.bikeridediary.global.auth.jwt;

import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

// JWT 토큰 생성 및 검증 (액세스 토큰: 1시간, 리프레시 토큰: 30일)
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry * 1000L;   // 초를 밀리초로 변환
        this.refreshTokenExpiry = refreshTokenExpiry * 1000L;
    }

    // 액세스 토큰 생성
    public String generateAccessToken(UUID userId) {
        return buildToken(userId.toString(), accessTokenExpiry);
    }

    // 리프레시 토큰 생성
    public String generateRefreshToken(UUID userId) {
        return buildToken(userId.toString(), refreshTokenExpiry);
    }

    // 게스트용 리프레시 토큰 (1년)
    public String generateGuestRefreshToken(UUID userId) {
        return buildToken(userId.toString(), 365L * 24 * 60 * 60 * 1000);
    }

    private String buildToken(String subject, long expiry) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiry))
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public UUID extractUserId(String token) {
        String subject = parseClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    // 토큰 유효성 검증 및 유효하면 true 반환
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
