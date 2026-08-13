package com.bikeridediary.domain.user_report.service;

import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.domain.user.repository.UserRepository;
import com.bikeridediary.domain.user_report.dto.UserReportRequest;
import com.bikeridediary.domain.user_report.entity.UserReportEntity;
import com.bikeridediary.domain.user_report.repository.UserReportRepository;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

import static com.bikeridediary.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportService {
    private final UserReportRepository userReportRepository;
    private final UserRepository userRepository;

    @Transactional
    public void report(UserReportRequest request, UUID userId) {
        UserEntity user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

        userReportRepository.save(UserReportEntity.create(request, user));
    }


}
