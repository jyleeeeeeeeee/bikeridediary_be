package com.bikeridediary.domain.auth;

import com.bikeridediary.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * OAuth2 소셜 로그인 및 일반 이메일 로그인, 토큰 관리 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 일반 이메일 회원가입 엔드포인트.
     * <p>
     * Request:
     * POST /api/v1/auth/signup
     * {
     * "email": "test@example.com",
     * "password": "SecurePass123!",
     * "nickname": "테스트"
     * }
     * <p>
     * Response:
     * {
     * "success": true,
     * "data": {
     * "accessToken": "eyJ...",
     * "refreshToken": "eyJ...",
     * "user": { "id": "...", "email": "...", "nickname": "..." }
     * }
     * }
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * 일반 이메일 로그인 엔드포인트.
     *
     * Request:
     * POST /api/v1/auth/login
     * {
     *   "email": "test@example.com",
     *   "password": "SecurePass123!"
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "accessToken": "eyJ...",
     *     "refreshToken": "eyJ...",
     *     "user": { "id": "...", "email": "...", "nickname": "..." }
     *   }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithEmail(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginWithEmail(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    /**
     * 소셜 로그인 엔드포인트.
     *
     * Request:
     * - GET /api/v1/auth/login/{provider}?code=...  (Kakao)
     * - GET /api/v1/auth/login/{provider}?identityToken=...  (Google, Apple)
     *
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "accessToken": "eyJ...",
     *     "refreshToken": "eyJ...",
     *     "user": { "id": "...", "nickname": "...", ... }
     *   }
     * }
     */
    @PostMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @PathVariable String provider,
            @Valid @RequestBody AuthLoginRequest request
    ) {

        // Authorization Code 또는 Identity Token 결정
        // Kakao -> code / Google, Apple -> identityToken
        String credential = request.code() != null
                ? request.code()
                : request.identityToken();

        AuthResponse response = authService.login(provider, credential);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 토큰 갱신 엔드포인트.
     *
     * Request:
     * POST /api/v1/auth/refresh
     * {
     *   "refreshToken": "eyJ..."
     * }
     *
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "accessToken": "eyJ...",
     *     "refreshToken": "eyJ..."
     *   }
     * }
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 로그아웃 엔드포인트.
     *
     * Request:
     * POST /api/v1/auth/logout
     * (Authorization 헤더에 Bearer token 필수)
     *
     * Response:
     * {
     *   "success": true
     * }
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // 인증된 사용자의 ID 추출
        UUID userId = UUID.fromString(userDetails.getUsername());

        // 로그아웃 처리 (Refresh Token 삭제)
        authService.logout(userId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
