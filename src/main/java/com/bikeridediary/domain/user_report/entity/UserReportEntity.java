package com.bikeridediary.domain.user_report.entity;

import com.bikeridediary.domain.common.entity.BaseEntity;
import com.bikeridediary.domain.place.entity.PlaceEntity;
import com.bikeridediary.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

// 유저 제보(버그/장소 삭제/기타) 엔티티.
@Entity
@Table(
        name = "user_reports",
        indexes = {
                @Index(name = "idx_user_reports_user_id", columnList = "user_id"),
                @Index(name = "idx_user_reports_status_created_at", columnList = "status, created_at DESC")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReportEntity extends BaseEntity {

    // 제보 ID (UUID) — Hibernate가 자동 생성 (UserEntity와 동일 패턴)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval). insertable/updatable=false로
    // 애플리케이션 관여 없이 DB 시퀀스가 담당. INSERT 후 값을 다시 읽어오도록 @Generated.
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    // 요청 제목
    @Column(name = "title", nullable = false)
    private String title;

    // 요청 종류
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    // 요청 내용 (PLACE_DELETE 시 이유 문자열, 대상 place는 target_place_id 참조)
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // 대상 place (report_type=PLACE_DELETE 등에서 사용, 그 외 null)
    // ON DELETE SET NULL: place 삭제돼도 제보 히스토리 유지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_place_id")
    private PlaceEntity targetPlace;

    // 처리 상태 : REPORTED(default), PROCEEDING, DONE, REJECT
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.REPORTED;

    // 관리자 처리 응답 (반려 사유 or 처리 결과)
    @Column(name = "reply", columnDefinition = "TEXT")
    private String reply;

    // 제보자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 검토한 어드민
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserEntity reviewer;

    // 검토 시각
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    // 신규 제보 생성. id는 @GeneratedValue, status는 필드 기본값(REPORTED) 사용.
    public static UserReportEntity create(
            String title,
            ReportType reportType,
            String content,
            PlaceEntity targetPlace,   // PLACE_DELETE 아니면 null
            UserEntity userEntity
    ) {
        UserReportEntity report = new UserReportEntity();
        report.title = title;
        report.reportType = reportType;
        report.content = content;
        report.targetPlace = targetPlace;
        report.userEntity = userEntity;
        return report;
    }

    // 관리자 상태 변경
    public void updateStatus(ReportStatus status, String reply, UserEntity reviewer) {
        this.status = status;
        this.reply = reply;
        this.reviewer = reviewer;
        this.reviewedAt = LocalDateTime.now();
        if (status == ReportStatus.DONE || status == ReportStatus.REJECT) {
            this.endedAt = LocalDateTime.now();
        }
    }
}
