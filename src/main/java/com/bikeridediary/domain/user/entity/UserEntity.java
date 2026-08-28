package com.bikeridediary.domain.user.entity;

import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 사용자 엔티티 - OAuth2(카카오, 구글, 애플, 네이버)와 이메일 로그인을 지원하며 프로필 및 알림 정보를 관리
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
public class UserEntity extends BaseEntity {

    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval). insertable/updatable=false로
    // 애플리케이션 관여 없이 DB 시퀀스가 담당. INSERT 후 값을 다시 읽어오도록 @Generated.
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    // 사용자 ID (UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
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

    // 사용자 권한(USER : 일반 사용자, ADMIN : 운영자)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;

    // 사용자가 소유한 바이크 목록 (양방향 One-To-Many)
    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.PERSIST, orphanRemoval = false)
    @JsonManagedReference
    private List<BikeEntity> bikes = new ArrayList<>();

    // OAuth2를 통한 사용자 엔티티 생성
    public static UserEntity create(String provider, String providerId, String email, String nickname) {
        UserEntity userEntity = new UserEntity();
        userEntity.provider = provider;
        userEntity.providerId = providerId;
        userEntity.email = email;
        userEntity.nickname = nickname;
        return userEntity;
    }

    // 이메일 회원가입으로 사용자 엔티티 생성 (암호화된 비밀번호 사용)
    public static UserEntity createWithEmail(String email, String hashedPassword, String nickname) {

        UserEntity userEntity = new UserEntity();
        userEntity.provider = "email";    // 일반 회원 provider : "email"

        byte[] bytes = email.getBytes(StandardCharsets.UTF_8);
        
        /*
        // SHA-256 해시 직접 구현 방식
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            // 64자리 hex 문자열
            userEntity.providerId = HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        */

        // (UUID v3, MD5 기반)
        userEntity.providerId = UUID.nameUUIDFromBytes(bytes).toString();

        userEntity.email = email;
        userEntity.password = hashedPassword;     // BCrypt로 암호화된 비밀번호
        userEntity.nickname = nickname;
        return userEntity;
    }

    // 프로필 정보 수정 (닉네임, 프로필 이미지)
    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    // 프로필 정보 수정 (닉네임, 프로필 이미지)
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // Firebase FCM 푸시 알림 토큰 갱신
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
