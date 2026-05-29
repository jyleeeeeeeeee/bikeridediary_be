package com.bikeridediary.domain.user.service;

import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// JWT 인증용 UserDetailsService 구현 (JWT 토큰에서 추출한 userId로 사용자 조회)
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        return this.loadUserByUsername(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByIdAndDeletedAtIsNull(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new org.springframework.security.core.userdetails.User(
                userEntity.getId().toString(),
                "",  // 소셜 로그인 전용이므로 비밀번호 없음
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}