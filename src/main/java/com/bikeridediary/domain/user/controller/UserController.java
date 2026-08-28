package com.bikeridediary.domain.user.controller;

import com.bikeridediary.domain.user.dto.NicknameUpdateRequest;
import com.bikeridediary.domain.user.dto.UserResponse;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.domain.user.service.CustomUserDetailsService;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.exception.BusinessException;

import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static com.bikeridediary.global.exception.ErrorCode.*;


@Tag(name = "사용자", description = "사용자 정보 조회")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;

    @Operation(summary = "내 정보 조회 (JWT 기반)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserEntity user = customUserDetailsService.getMyInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
    }

    @Operation(summary = "닉네임 변경")
    @PatchMapping("/me/nickname")
    public ResponseEntity<ApiResponse<UserResponse>> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        UserEntity user = customUserDetailsService.updateNickname(userDetails.getUserId(), request.nickname());
        return ResponseEntity.ok(ApiResponse.ok(UserResponse.from(user)));
    }
}