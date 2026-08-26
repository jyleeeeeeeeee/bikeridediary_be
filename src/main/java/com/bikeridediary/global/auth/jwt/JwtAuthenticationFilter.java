package com.bikeridediary.global.auth.jwt;

import com.bikeridediary.domain.user.entity.UserRole;
import com.bikeridediary.global.auth.CustomUserDetails;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import com.bikeridediary.global.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// JWT 인증 필터. 요청마다 한 번 실행되어 Authorization 헤더에서 Bearer 토큰을 추출하고,
// 토큰을 검증한 후 SecurityContext에 인증 정보를 설정한다.
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.isValid(token)) {
                    UUID userId = jwtTokenProvider.extractUserId(token);
                    UserRole role = jwtTokenProvider.extractUserRole(token);
                    CustomUserDetails userDetails = new CustomUserDetails(userId, role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (BusinessException e) {
                if (ErrorCode.AUTH_EXPIRED_TOKEN == e.getErrorCode()) {
                    log.info("Business exception: {}", e.getMessage());
                } else {
                    log.warn("Business exception: {}", e.getMessage());
                }

                response.setStatus(e.getHttpStatus().value());
                response.setContentType("application/json;charset=UTF-8");
                ApiResponse<Void> body = ApiResponse.fail(e.getErrorCode().getCode(), e.getMessage());
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
