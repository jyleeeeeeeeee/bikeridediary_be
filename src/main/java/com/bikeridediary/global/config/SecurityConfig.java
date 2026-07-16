package com.bikeridediary.global.config;

import com.bikeridediary.global.auth.jwt.JwtAuthenticationFilter;
import com.bikeridediary.global.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// Spring Security 설정 (상태 비저장 JWT 기반, 공개 엔드포인트 제외 인증 필수)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final String[] PERMIT_ALL_ENDPOINTS = {
            "/api/v1/weathers/**",
            "/api/v1/auth/**",
            "/api/v1/stations/**",
            "/api/v1/places/**",
            "/swagger-ui/**",
            "/api-docs/**",
            "/logos/**",
    };

    private final String[] GET_PERMIT_ALL_ENDPOINTS = {
            "/api/v1/courses",
            "/api/v1/bike-models/**",
    };

    private final String[] AUTHENTICATED_ENDPOINTS = {
            "/files/**"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 — Flutter 웹 앱(다른 포트)에서의 API 호출 허용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화 (Stateless REST API)
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless 세션 (서버 측 세션 저장 안 함)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 인증 실패 시 403 대신 401 반환
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"status\":401,\"message\":\"Unauthorized\"}");
                        })
                )

                // URL 기반 인가
                .authorizeHttpRequests(auth -> auth
                        // 공개 엔드포인트
                        .requestMatchers(PERMIT_ALL_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, GET_PERMIT_ALL_ENDPOINTS).permitAll()
                        // 나머지는 모두 인증 필요
                        .requestMatchers(AUTHENTICATED_ENDPOINTS).authenticated()
                        .anyRequest().authenticated()
                )

                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 개발 환경: Flutter 웹 앱의 origin 허용
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://192.168.*:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
