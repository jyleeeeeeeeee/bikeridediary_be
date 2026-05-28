package com.bikeridediary.domain.auth;

import com.bikeridediary.domain.user.UserEntity;
import com.bikeridediary.domain.user.UserRepository;
import com.bikeridediary.global.auth.jwt.JwtTokenProvider;
import com.bikeridediary.global.auth.oauth2.*;
import com.bikeridediary.global.auth.token.RefreshTokenRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private KakaoProvider kakaoProvider;

    @Mock
    private GoogleProvider googleProvider;

    @Mock
    private AppleProvider appleProvider;

    @Mock
    private NaverProvider naverProvider;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private String accessToken;
    private String refreshToken;
    private UserEntity testUserEntity;

    private String hashedPassword;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accessToken = "test_access_token";
        refreshToken = "test_refresh_token";
        testUserEntity = UserEntity.create("kakao", "123456", "test@example.com", "테스트");
        setUserIdReflection(testUserEntity, userId);

        // 실제 BCryptPasswordEncoder 생성 및 주입
        passwordEncoder = new BCryptPasswordEncoder();
        ReflectionTestUtils.setField(authService, "passwordEncoder", passwordEncoder);

        // 실제 BCrypt 해시 생성
        hashedPassword = passwordEncoder.encode("SecurePass123!");

    }

    private void setUserIdReflection(UserEntity userEntity, UUID id) {
        try {
            var field = userEntity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(userEntity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("login with Kakao - 새로운 사용자")
    void loginWithKakao_NewUser() {
        String code = "kakao_auth_code";
        OAuth2UserInfo userInfo = new OAuth2UserInfo("123456", "test@example.com", "테스트", "https://example.com/image.jpg");
        UUID newUserId = UUID.randomUUID();

        when(kakaoProvider.getUserInfo(code))
                .thenReturn(userInfo);
        when(userRepository.findByProviderAndProviderId("kakao", "123456"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity userEntity = invocation.getArgument(0);
                    setUserIdReflection(userEntity, newUserId);
                    return userEntity;
                });
        when(jwtTokenProvider.generateAccessToken(newUserId))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(newUserId))
                .thenReturn(refreshToken);

        AuthResponse response = authService.login("kakao", code);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.user()).isNotNull();

        verify(userRepository).save(any(UserEntity.class));
        verify(refreshTokenRepository).save(newUserId, refreshToken);
    }

    @Test
    @DisplayName("login with Kakao - 기존 사용자")
    void loginWithKakao_ExistingUser() {
        String code = "kakao_auth_code";
        OAuth2UserInfo userInfo = new OAuth2UserInfo("123456", "test@example.com", "테스트", "https://example.com/image.jpg");

        when(kakaoProvider.getUserInfo(code))
                .thenReturn(userInfo);
        when(userRepository.findByProviderAndProviderId("kakao", "123456"))
                .thenReturn(Optional.of(testUserEntity));
        when(jwtTokenProvider.generateAccessToken(testUserEntity.getId()))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(testUserEntity.getId()))
                .thenReturn(refreshToken);

        AuthResponse response = authService.login("kakao", code);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(refreshTokenRepository).save(testUserEntity.getId(), refreshToken);
    }

    @Test
    @DisplayName("login - 프로필 이미지 설정")
    void login_WithProfileImage() {
        String code = "kakao_auth_code";
        OAuth2UserInfo userInfo = new OAuth2UserInfo("123456", "test@example.com", "테스트", "https://example.com/image.jpg");
        UUID newUserId = UUID.randomUUID();

        when(kakaoProvider.getUserInfo(code))
                .thenReturn(userInfo);
        when(userRepository.findByProviderAndProviderId("kakao", "123456"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity userEntity = invocation.getArgument(0);
                    setUserIdReflection(userEntity, newUserId);
                    return userEntity;
                });
        when(jwtTokenProvider.generateAccessToken(newUserId))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(newUserId))
                .thenReturn(refreshToken);

        authService.login("kakao", code);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getProfileImageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("login - 닉네임 생성 (name 사용)")
    void login_NicknameFromName() {
        String code = "kakao_auth_code";
        OAuth2UserInfo userInfo = new OAuth2UserInfo("123456", "test@example.com", "카카오사용자", "https://example.com/image.jpg");
        UUID newUserId = UUID.randomUUID();

        when(kakaoProvider.getUserInfo(code))
                .thenReturn(userInfo);
        when(userRepository.findByProviderAndProviderId("kakao", "123456"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity userEntity = invocation.getArgument(0);
                    setUserIdReflection(userEntity, newUserId);
                    return userEntity;
                });
        when(jwtTokenProvider.generateAccessToken(newUserId))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(newUserId))
                .thenReturn(refreshToken);

        authService.login("kakao", code);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo("카카오사용자");
    }

    @Test
    @DisplayName("login - 닉네임 생성 (email 사용)")
    void login_NicknameFromEmail() {
        String code = "kakao_auth_code";
        OAuth2UserInfo userInfo = new OAuth2UserInfo("123456", "testuser@example.com", null, null);
        UUID newUserId = UUID.randomUUID();

        when(kakaoProvider.getUserInfo(code))
                .thenReturn(userInfo);
        when(userRepository.findByProviderAndProviderId("kakao", "123456"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity userEntity = invocation.getArgument(0);
                    setUserIdReflection(userEntity, newUserId);
                    return userEntity;
                });
        when(jwtTokenProvider.generateAccessToken(newUserId))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(newUserId))
                .thenReturn(refreshToken);

        authService.login("kakao", code);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getNickname()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("login - 닉네임 생성 (기본값)")
    void login_NicknameFromDefault() {
        String code = "kakao_auth_code";
        OAuth2UserInfo userInfo = new OAuth2UserInfo("12345678901234567890", null, null, null);
        UUID newUserId = UUID.randomUUID();

        when(kakaoProvider.getUserInfo(code))
                .thenReturn(userInfo);
        when(userRepository.findByProviderAndProviderId("kakao", "12345678901234567890"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity userEntity = invocation.getArgument(0);
                    setUserIdReflection(userEntity, newUserId);
                    return userEntity;
                });
        when(jwtTokenProvider.generateAccessToken(newUserId))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(newUserId))
                .thenReturn(refreshToken);

        authService.login("kakao", code);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getNickname()).startsWith("사용자_");
    }

    @Test
    @DisplayName("login - 지원하지 않는 제공자")
    void login_UnsupportedProvider() {
        String code = "invalid_code";

        assertThatThrownBy(() -> authService.login("invalid_provider", code))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_UNSUPPORTED_PROVIDER);
    }

    @Test
    @DisplayName("refresh - 토큰 갱신 성공")
    void refresh_Success() {
        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";

        when(jwtTokenProvider.extractUserId(refreshToken))
                .thenReturn(userId);
        when(refreshTokenRepository.isValid(userId, refreshToken))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(userId))
                .thenReturn(newAccessToken);
        when(jwtTokenProvider.generateRefreshToken(userId))
                .thenReturn(newRefreshToken);

        TokenResponse response = authService.refresh(refreshToken);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(newAccessToken);
        assertThat(response.refreshToken()).isEqualTo(newRefreshToken);

        verify(refreshTokenRepository).save(userId, newRefreshToken);
    }

    @Test
    @DisplayName("refresh - 유효하지 않은 토큰")
    void refresh_InvalidToken() {
        when(jwtTokenProvider.extractUserId(refreshToken))
                .thenReturn(userId);
        when(refreshTokenRepository.isValid(userId, refreshToken))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("refresh - 토큰 파싱 실패")
    void refresh_InvalidTokenFormat() {
        when(jwtTokenProvider.extractUserId(refreshToken))
                .thenThrow(new RuntimeException("Invalid token format"));

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("logout - 토큰 삭제")
    void logout_Success() {
        authService.logout(userId);

        verify(refreshTokenRepository).delete(userId);
    }

    @Test
    @DisplayName("signup - 회원가입 성공")
    void signup_Success() {
        SignupRequest request = new SignupRequest(
                "newuser@example.com",
                "SecurePass123!",
                "신규사용자"
        );
        UUID newUserId = UUID.randomUUID();

        when(userRepository.existsByEmailAndDeletedAtIsNull("newuser@example.com"))
                .thenReturn(false);  // 이메일 중복 아님
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity userEntity = invocation.getArgument(0);
                    setUserIdReflection(userEntity, newUserId);
                    return userEntity;
                });
        when(jwtTokenProvider.generateAccessToken(newUserId))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(newUserId))
                .thenReturn(refreshToken);

        AuthResponse response = authService.signup(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.user()).isNotNull();

        verify(userRepository).save(any(UserEntity.class));
        verify(refreshTokenRepository).save(newUserId, refreshToken);
    }


    @Test
    @DisplayName("signup - 이메일 중복")
    void signup_EmailAlreadyExists() {
        SignupRequest request = new SignupRequest(
                "existing@example.com",
                "SecurePass123!",
                "사용자"
        );

        when(userRepository.existsByEmailAndDeletedAtIsNull("existing@example.com"))
                .thenReturn(true);  // 이메일 이미 존재

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_USER_ALREADY_EXISTS);

        verify(userRepository, never()).save(any(UserEntity.class));
    }


    @Test
    @DisplayName("signup - 잘못된 이메일 형식")
    void signup_InvalidEmail() {
        SignupRequest request = new SignupRequest(
                "invalid-email",  // @ 없음
                "SecurePass123!",
                "사용자"
        );

        when(userRepository.existsByEmailAndDeletedAtIsNull("invalid-email"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_EMAIL_INVALID_FORMAT);

        verify(userRepository, never()).save(any(UserEntity.class));
    }


    @Test
    @DisplayName("signup - 비밀번호 너무 짧음 (8자 미만)")
    void signup_WeakPassword_TooShort() {
        SignupRequest request = new SignupRequest(
                "newuser@example.com",
                "Short1!",  // 7자
                "사용자"
        );

        when(userRepository.existsByEmailAndDeletedAtIsNull("newuser@example.com"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_PASSWORD_TOO_WEAK);

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("signup - 비밀번호에 숫자 없음")
    void signup_WeakPassword_NoDigits() {
        SignupRequest request = new SignupRequest(
                "newuser@example.com",
                "NoDigits!",  // 숫자 없음
                "사용자"
        );

        when(userRepository.existsByEmailAndDeletedAtIsNull("newuser@example.com"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_PASSWORD_TOO_WEAK);

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("signup - 비밀번호에 특수문자 없음")
    void signup_WeakPassword_NoSpecialChar() {
        SignupRequest request = new SignupRequest(
                "newuser@example.com",
                "NoSpecial1",  // 특수문자 없음
                "사용자"
        );

        when(userRepository.existsByEmailAndDeletedAtIsNull("newuser@example.com"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_PASSWORD_TOO_WEAK);

        verify(userRepository, never()).save(any(UserEntity.class));
    }


    @Test
    @DisplayName("loginWithEmail - 로그인 성공")
    void loginWithEmail_Success() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "SecurePass123!"
        );
        UserEntity emailUserEntity = UserEntity.createWithEmail("test@example.com", hashedPassword, "테스트");
        setUserIdReflection(emailUserEntity, userId);

        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                .thenReturn(Optional.of(emailUserEntity));
        when(jwtTokenProvider.generateAccessToken(userId))
                .thenReturn(accessToken);
        when(jwtTokenProvider.generateRefreshToken(userId))
                .thenReturn(refreshToken);

        AuthResponse response = authService.loginWithEmail(request);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        verify(refreshTokenRepository).save(userId, refreshToken);
    }


    @Test
    @DisplayName("loginWithEmail - 사용자 없음")
    void loginWithEmail_UserNotFound() {
        LoginRequest request = new LoginRequest(
                "nonexistent@example.com",
                "SecurePass123!"
        );

        when(userRepository.findByEmailAndDeletedAtIsNull("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginWithEmail(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);
    }


    @Test
    @DisplayName("loginWithEmail - 비밀번호 불일치")
    void loginWithEmail_InvalidPassword() {
        LoginRequest request = new LoginRequest(
                "test@example.com",
                "WrongPassword123!"
        );
        UserEntity emailUserEntity = UserEntity.createWithEmail("test@example.com", hashedPassword, "테스트");
        setUserIdReflection(emailUserEntity, userId);

        when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                .thenReturn(Optional.of(emailUserEntity));

        assertThatThrownBy(() -> authService.loginWithEmail(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AUTH_INVALID_CREDENTIALS);

        verify(refreshTokenRepository, never()).save(any(UUID.class), anyString());
    }


}
