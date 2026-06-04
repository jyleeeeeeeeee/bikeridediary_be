package com.bikeridediary.domain.auth.controller;

import com.bikeridediary.domain.auth.dto.*;
import com.bikeridediary.domain.auth.service.AuthService;
import com.bikeridediary.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.bikeridediary.global.auth.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// OAuth2 소셜 로그인 및 이메일 로그인, 토큰 관리 API 컨트롤러
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 이메일 회원가입 (이메일, 비밀번호, 닉네임으로 신규 계정 생성)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    // 이메일 로그인 (이메일과 비밀번호로 인증)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithEmail(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginWithEmail(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    // OAuth2 소셜 로그인 (카카오: code, 구글/애플: identityToken)
    @PostMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @PathVariable String provider,
            @Valid @RequestBody AuthLoginRequest request
    ) {

        // Authorization Code 또는 Identity Token 판정 (카카오 → code, 구글/애플 → identityToken)
        String credential = request.code() != null
                ? request.code()
                : request.identityToken();

        AuthResponse response = authService.login(provider, credential);

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 리프레시 토큰으로 새 액세스 토큰 발급
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // 로그아웃 (리프레시 토큰 삭제)
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 인증된 사용자 ID 추출
        UUID userId = userDetails.getUserId();

        // 로그아웃 처리 (리프레시 토큰 삭제)
        authService.logout(userId);

        return ResponseEntity.ok(ApiResponse.ok());
    }
}
