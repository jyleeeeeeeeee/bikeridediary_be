package com.bikeridediary.domain.auth.controller;

import com.bikeridediary.domain.auth.dto.*;
import com.bikeridediary.domain.auth.service.AuthService;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.bikeridediary.global.auth.CustomUserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "게스트 가입", description = "랜덤 데이터로 게스트 계정을 생성합니다. 리프레시 토큰 1년.")
    @PostMapping("/guest")
    public ResponseEntity<ApiResponse<AuthResponse>> guestSignup() {
        AuthResponse response = authService.guestSignup();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "이메일 회원가입", description = "이메일, 비밀번호, 닉네임으로 신규 계정을 생성합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @Operation(summary = "이메일 로그인", description = "이메일과 비밀번호로 인증합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithEmail(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.loginWithEmail(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    @Operation(summary = "소셜 로그인", description = "OAuth2 소셜 로그인. 카카오는 Access Token, 구글/애플은 Identity Token을 전달합니다.")
    @PostMapping("/login/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @PathVariable String provider,
            @Valid @RequestBody AuthLoginRequest request
    ) {
        AuthResponse response = authService.login(provider, request.credential(), request.name());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 새 액세스 토큰을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 삭제하여 로그아웃합니다.")
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
