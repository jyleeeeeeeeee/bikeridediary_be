package com.bikeridediary.domain.user_report.entity;

import com.bikeridediary.domain.common.entity.BaseEntity;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user_report.dto.UserReportRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReportEntity extends BaseEntity {
    // 조회용 친숙 번호 (자동 증가, DB DEFAULT nextval). insertable/updatable=false로
    // 애플리케이션 관여 없이 DB 시퀀스가 담당. INSERT 후 값을 다시 읽어오도록 @Generated.
    @Column(name = "no", insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long no;

    // ID (UUID)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    // 요청 제목
    @Column(name = "title", nullable = false)
    private String title;

    // 요청 종류
    @Column(name = "report_type", nullable = false)
    private String reportType;

    /**
     * 요청 내용
     * reportType : PLACE_DELETE -> place_id
     * reportType : BUG_REPORT, ETC -> 문자열
     */
    @Column(name = "content", nullable = false)
    private String content;

    // 처리상태 : REPORTED, PROCEEDING, DONE, REJECT
    @Column(name = "status", nullable = false)
    private String status = "REPORTED";

    // 처리상태 : REPORTED, PROCEEDING, DONE, REJECT
    @Column(name = "reply")
    private String reply = null;

    // 제보자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    public static UserReportEntity create(UserReportRequest request, UserEntity user) {
        UserReportEntity report = new UserReportEntity();
        report.id = UUID.randomUUID();
        report.title = request.title();
        report.reportType = request.reportType();
        report.content = request.content();
        report.userEntity = user;
        return report;
    }
}
