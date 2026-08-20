package com.bikeridediary.domain.user_report.service;

import com.bikeridediary.domain.place.entity.PlaceEntity;
import com.bikeridediary.domain.place.repository.PlaceRepository;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.domain.user_report.dto.UserReportRequest;
import com.bikeridediary.domain.user_report.dto.UserReportUpdateRequest;
import com.bikeridediary.domain.user_report.entity.ReportStatus;
import com.bikeridediary.domain.user_report.entity.ReportType;
import com.bikeridediary.domain.user_report.entity.UserReportEntity;
import com.bikeridediary.domain.user_report.repository.UserReportRepository;
import com.bikeridediary.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.bikeridediary.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportService {
    private final UserReportRepository userReportRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public void report(UserReportRequest request, UUID userId) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
        PlaceEntity targetPlace = null;
        if (request.reportType() == ReportType.PLACE_DELETE) {
            UUID targetPlaceId = request.targetPlaceId();
            if(targetPlaceId == null) throw new BusinessException(PLACE_NOT_FOUND);
            targetPlace = placeRepository.findByIdAndDeletedAtIsNull(targetPlaceId)
                    .orElseThrow(() -> new BusinessException(PLACE_NOT_FOUND));
        }

        userReportRepository.save(UserReportEntity.create(
                request.title(),
                request.reportType(),
                request.content(),
                targetPlace,
                user
        ));
    }

    @Transactional
    public void updateReport(UUID reportId, UUID reviewerId, UserReportUpdateRequest request) {
        UserReportEntity report = userReportRepository.findByIdAndDeletedAtIsNull(reportId)
                .orElseThrow(() -> new BusinessException(REPORT_NOT_FOUND));

        ReportStatus currentStatus = report.getStatus();
        if(currentStatus == ReportStatus.DONE || currentStatus == ReportStatus.REJECT) {
            throw new BusinessException(REPORT_ALREADY_REVIEWED);
        }
        UserEntity reviewer = userRepository.findByIdAndDeletedAtIsNull(reviewerId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        report.updateStatus(request.status(), request.reply(), reviewer);
    }




}
