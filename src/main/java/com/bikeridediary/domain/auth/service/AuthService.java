package com.bikeridediary.domain.auth.service;

import com.bikeridediary.domain.auth.dto.AuthResponse;
import com.bikeridediary.domain.auth.dto.LoginRequest;
import com.bikeridediary.domain.auth.dto.SignupRequest;
import com.bikeridediary.domain.auth.dto.TokenResponse;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.domain.user.dto.UserResponse;
import com.bikeridediary.global.auth.jwt.JwtTokenProvider;
import com.bikeridediary.global.auth.oauth2.*;
import com.bikeridediary.global.auth.token.RefreshTokenRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

// OAuth2 소셜 로그인 및 토큰 관리 서비스
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // OAuth2 제공자들
    private final KakaoProvider kakaoProvider;
    private final GoogleProvider googleProvider;
    private final AppleProvider appleProvider;
    private final NaverProvider naverProvider;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // Step 1: 이메일 중복 확인
        String email = request.email();
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BusinessException(ErrorCode.AUTH_USER_ALREADY_EXISTS);
        }

        // Step 2: 이메일 형식 검증
        validateEmail(email);

        // Step 3: 비밀번호 강도 검증
        String password = request.password();
        validatePassword(password);

        // Step 4: 비밀번호 암호화
        String hashedPassword = passwordEncoder.encode(password);

        UserEntity newUserEntity = UserEntity.createWithEmail(email, hashedPassword, request.nickname());
        UserEntity savedUserEntity = userRepository.save(newUserEntity);

        UUID id = savedUserEntity.getId();
        String accessToken = jwtTokenProvider.generateAccessToken(id);
        String refreshToken = jwtTokenProvider.generateRefreshToken(id);

        refreshTokenRepository.save(id, refreshToken);

        log.info("User registered - email: {}", email);

        return new AuthResponse(accessToken, refreshToken, UserResponse.from(savedUserEntity));
    }

    private static void validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_INVALID_FORMAT);
        }
    }

    private void validatePassword(String password) {
        // 최소 8자
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_TOO_WEAK);
        }
        // 영문 포함
        if (!password.matches(".*[a-zA-Z].*")) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_TOO_WEAK);
        }
        // 숫자 포함
        if (!password.matches(".*\\d.*")) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_TOO_WEAK);
        }
        // 특수문자 포함
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?/\\\\|`~].*")) {
            throw new BusinessException(ErrorCode.AUTH_PASSWORD_TOO_WEAK);
        }
    }


    // OAuth2 소셜 로그인 처리
    // 흐름: 1. Authorization Code 또는 Identity Token으로 사용자 정보 조회
    //       2. 기존 사용자 확인, 없으면 신규 가입
    //       3. JWT 발급 (Access Token + Refresh Token)
    //       4. Refresh Token을 Redis에 저장
    //       5. 사용자 정보와 함께 응답
    @Transactional
    public AuthResponse login(String provider, String credential) {
        log.info("Social login attempt - provider: {}", provider);

        // Step 1: 제공자별로 사용자 정보 조회
        OAuth2UserInfo userInfo = getUserInfoByProvider(provider, credential);

        // Step 2: 기존 사용자 조회 또는 신규 가입
        UserEntity userEntity = findOrCreateUser(provider, userInfo);

        // Step 3: JWT 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(userEntity.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userEntity.getId());

        // Step 4: Refresh Token 저장 (Redis)
        refreshTokenRepository.save(userEntity.getId(), refreshToken);

        log.info("Login successful - userId: {}, provider: {}", userEntity.getId(), provider);

        // Step 5: 응답 생성
        return new AuthResponse(
                accessToken,
                refreshToken,
                UserResponse.from(userEntity)
        );
    }

    @Transactional
    public AuthResponse loginWithEmail(LoginRequest request) {
        // Step 1: 이메일로 사용자 조회
        UserEntity userEntity = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS));

        // Step 2: 비밀번호 검증
        if(!passwordEncoder.matches(request.password(), userEntity.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // Step 3: JWT 토큰 발급
        UUID id = userEntity.getId();
        String accessToken = jwtTokenProvider.generateAccessToken(id);
        String refreshToken = jwtTokenProvider.generateRefreshToken(id);

        // Step 4: RefreshToken Redis 저장
        refreshTokenRepository.save(id, refreshToken);

        log.info("Login successful - userId: {}, email: {}", id, userEntity.getEmail());

        // Step 5: 응답 생성
        return new AuthResponse(
                accessToken,
                refreshToken,
                UserResponse.from(userEntity)
        );
    }

    // 제공자별로 사용자 정보 조회
    private OAuth2UserInfo getUserInfoByProvider(String provider, String credential) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakaoProvider.getUserInfo(credential);
            case "google" -> googleProvider.getUserInfo(credential);
            case "apple" -> appleProvider.getUserInfo(credential);
            default -> throw new BusinessException(ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
        };
    }

    // 기존 사용자 조회 또는 신규 가입
    // provider + providerId 조합이 고유 키
    private UserEntity findOrCreateUser(String provider, OAuth2UserInfo userInfo) {
        Optional<UserEntity> existing = userRepository.findByProviderAndProviderId(
                provider,
                userInfo.getId()
        );

        if (existing.isPresent()) {
            return existing.get();
        }

        // 신규 가입: User 엔티티 생성
        UserEntity newUserEntity = UserEntity.create(
                provider,
                userInfo.getId(),
                userInfo.getEmail(),
                generateNickname(userInfo, provider)
        );

        // 프로필 이미지 설정 (있는 경우)
        if (userInfo.getPicture() != null) {
            newUserEntity.updateProfile(newUserEntity.getNickname(), userInfo.getPicture());
        }

        UserEntity saved = userRepository.save(newUserEntity);
        log.info("New user registered - provider: {}, providerId: {}", provider, userInfo.getId());

        return saved;
    }

    // 닉네임 생성 (제공자별로 다르게 처리)
    // 1. userInfo에서 name이 있으면 사용
    // 2. email이 있으면 @ 앞 부분 사용
    // 3. 없으면 기본값으로 "사용자_providerId"
    private String generateNickname(OAuth2UserInfo userInfo, String provider) {
        if (userInfo.getName() != null && !userInfo.getName().isEmpty()) {
            return userInfo.getName();
        }

        if (userInfo.getEmail() != null && userInfo.getEmail().contains("@")) {
            return userInfo.getEmail().split("@")[0];
        }

        return "사용자_" + userInfo.getId().substring(0, Math.min(8, userInfo.getId().length()));
    }

    // 토큰 갱신
    // Refresh Token의 유효성을 검증하고 새 Access Token 발급
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        try {
            // Step 1: Refresh Token에서 사용자 ID 추출
            UUID userId = jwtTokenProvider.extractUserId(refreshToken);

            // Step 2: Redis에서 저장된 토큰과 비교하여 유효성 검증
            if (!refreshTokenRepository.isValid(userId, refreshToken)) {
                throw new BusinessException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
            }

            // Step 3: 새로운 Access Token 발급
            String newAccessToken = jwtTokenProvider.generateAccessToken(userId);

            // Step 4: 새로운 Refresh Token 발급 및 저장
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
            refreshTokenRepository.save(userId, newRefreshToken);

            log.info("Token refreshed - userId: {}", userId);

            return new TokenResponse(newAccessToken, newRefreshToken);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new BusinessException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
        }
    }

    // 로그아웃
    // Refresh Token을 Redis에서 삭제하여 더 이상 사용 불가능하게 만듦
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.delete(userId);
        log.info("User logged out - userId: {}", userId);
    }
}
