package com.bikeridediary.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity.
 * Social login only - no password stored.
 * Supports both OAuth2(Kakao, Google, Apple, Naver) and email/Password login.
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_users_provider",
        columnNames = {"provider", "provider_id"}
    )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "nickname", "provider"})
public class User {

    // 사용자 ID (UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // OAuth2 제공자 (kakao, google, apple)
    @Column(nullable = false, length = 20)
    private String provider;

    // OAuth2 제공자에서 발급한 고유 ID
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    // 이메일 (nullable - Apple 사용자는 이메일 숨김 가능)
    @Column(length = 255)
    private String email;

    // 비밀번호 (일반 회원가입시만 사용, nullable)
    @Column(length = 255)
    private String password;
    
    // 닉네임
    @Column(nullable = false, length = 50)
    private String nickname;

    // 프로필 이미지 URL
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // Firebase FCM 푸시 알림 토큰
    @Column(name = "fcm_token")
    private String fcmToken;

    // 가입 일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 탈퇴 일시 (소프트 삭제)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Factory method
    public static User create(String provider, String providerId, String email, String nickname) {
        User user = new User();
        user.provider = provider;
        user.providerId = providerId;
        user.email = email;
        user.nickname = nickname;
        return user;
    }

    // 일반 이메일 회원가입용 메서드
    public static User createWithEmail(String email, String hashedPassword, String nickname) {
        User user = new User();
        user.provider = "email";    // 일반 회원 provider : "email"
        user.providerId = null;     // OAuth2가 아니므로 null 
        user.email = email;
        user.password = hashedPassword;     // BCrypt로 암호화된 비밀번호
        user.nickname = nickname;
        return user;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
