package com.bikeridediary.domain.maintenance;

import com.bikeridediary.domain.bike.entity.BikeCategory;
import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceScheduleUpdateRequest;
import com.bikeridediary.domain.maintenance.entity.MaintenanceScheduleEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import com.bikeridediary.domain.maintenance.repository.MaintenanceScheduleRepository;
import com.bikeridediary.domain.maintenance.service.MaintenanceScheduleService;
import com.bikeridediary.domain.user.entity.UserEntity;
import com.bikeridediary.global.exception.BusinessException;
import com.bikeridediary.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaintenanceScheduleService 단위 테스트")
class MaintenanceScheduleServiceTest {

    @Mock
    private MaintenanceScheduleRepository scheduleRepository;

    @Mock
    private BikeRepository bikeRepository;

    @InjectMocks
    private MaintenanceScheduleService scheduleService;

    private UUID userId;
    private UUID otherUserId;
    private UUID bikeId;
    private UUID scheduleId;
    private UserEntity testUser;
    private BikeEntity testBike;
    private MaintenanceScheduleEntity testSchedule;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        bikeId = UUID.randomUUID();
        scheduleId = UUID.randomUUID();

        testUser = UserEntity.create("kakao", "123456", "test@example.com", "테스트");
        setId(testUser, userId);

        testBike = BikeEntity.create(testUser, "Honda", "CB650R", 2024, BikeCategory.SPORT, 10000);
        setId(testBike, bikeId);

        testSchedule = MaintenanceScheduleEntity.create(
                testBike, MaintenanceType.ENGINE_OIL,
                5000, 6,
                8000, LocalDate.of(2026, 1, 1)
        );
        setId(testSchedule, scheduleId);
    }

    private void setId(Object entity, UUID id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ============ getSchedules ============

    @Test
    @DisplayName("getSchedules - 성공")
    void getSchedules_Success() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(scheduleRepository.findByBikeEntityIdAndDeletedAtIsNull(bikeId))
                .thenReturn(List.of(testSchedule));

        List<MaintenanceScheduleResponse> result = scheduleService.getSchedules(bikeId, userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).maintenanceType()).isEqualTo(MaintenanceType.ENGINE_OIL);
        assertThat(result.get(0).intervalKm()).isEqualTo(5000);
        verify(scheduleRepository).findByBikeEntityIdAndDeletedAtIsNull(bikeId);
    }

    @Test
    @DisplayName("getSchedules - 바이크 없음")
    void getSchedules_BikeNotFound() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.getSchedules(bikeId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_NOT_FOUND);
    }

    @Test
    @DisplayName("getSchedules - 다른 사용자의 바이크")
    void getSchedules_AccessDenied() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));

        assertThatThrownBy(() -> scheduleService.getSchedules(bikeId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_ACCESS_DENIED);
    }

    // ============ getSchedule ============

    @Test
    @DisplayName("getSchedule - 성공")
    void getSchedule_Success() {
        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.of(testSchedule));

        MaintenanceScheduleResponse result = scheduleService.getSchedule(scheduleId, userId);

        assertThat(result.id()).isEqualTo(scheduleId);
        assertThat(result.maintenanceType()).isEqualTo(MaintenanceType.ENGINE_OIL);
        assertThat(result.intervalMonths()).isEqualTo(6);
    }

    @Test
    @DisplayName("getSchedule - 정비 주기 없음")
    void getSchedule_NotFound() {
        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.getSchedule(scheduleId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("getSchedule - 다른 사용자의 정비 주기 접근 거부")
    void getSchedule_AccessDenied() {
        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> scheduleService.getSchedule(scheduleId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_SCHEDULE_ACCESS_DENIED);
    }

    // ============ createSchedule ============

    @Test
    @DisplayName("createSchedule - 성공")
    void createSchedule_Success() {
        MaintenanceScheduleCreateRequest request = new MaintenanceScheduleCreateRequest(
                bikeId, MaintenanceType.ENGINE_OIL, 5000, 6, 8000, LocalDate.of(2026, 1, 1)
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(scheduleRepository.existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull(bikeId, MaintenanceType.ENGINE_OIL))
                .thenReturn(false);
        when(scheduleRepository.save(any(MaintenanceScheduleEntity.class)))
                .thenReturn(testSchedule);

        MaintenanceScheduleResponse result = scheduleService.createSchedule(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.maintenanceType()).isEqualTo(MaintenanceType.ENGINE_OIL);
        verify(scheduleRepository).save(any(MaintenanceScheduleEntity.class));
    }

    @Test
    @DisplayName("createSchedule - 바이크 없음")
    void createSchedule_BikeNotFound() {
        MaintenanceScheduleCreateRequest request = new MaintenanceScheduleCreateRequest(
                bikeId, MaintenanceType.ENGINE_OIL, 5000, 6, null, null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.createSchedule(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_NOT_FOUND);

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSchedule - 다른 사용자의 바이크")
    void createSchedule_AccessDenied() {
        MaintenanceScheduleCreateRequest request = new MaintenanceScheduleCreateRequest(
                bikeId, MaintenanceType.ENGINE_OIL, 5000, 6, null, null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));

        assertThatThrownBy(() -> scheduleService.createSchedule(request, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_ACCESS_DENIED);

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSchedule - 동일 정비 종류 중복")
    void createSchedule_Duplicate() {
        MaintenanceScheduleCreateRequest request = new MaintenanceScheduleCreateRequest(
                bikeId, MaintenanceType.ENGINE_OIL, 5000, 6, 8000, LocalDate.of(2026, 1, 1)
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(scheduleRepository.existsByBikeEntityIdAndMaintenanceTypeAndDeletedAtIsNull(bikeId, MaintenanceType.ENGINE_OIL))
                .thenReturn(true);

        assertThatThrownBy(() -> scheduleService.createSchedule(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_SCHEDULE_DUPLICATE);

        verify(scheduleRepository, never()).save(any());
    }

    // ============ updateSchedule ============

    @Test
    @DisplayName("updateSchedule - 성공")
    void updateSchedule_Success() {
        MaintenanceScheduleUpdateRequest request = new MaintenanceScheduleUpdateRequest(
                10000, 12, 9500, LocalDate.of(2026, 6, 1)
        );

        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.of(testSchedule));

        MaintenanceScheduleResponse result = scheduleService.updateSchedule(scheduleId, request, userId);

        assertThat(result.intervalKm()).isEqualTo(10000);
        assertThat(result.intervalMonths()).isEqualTo(12);
        assertThat(result.lastMaintenanceMileage()).isEqualTo(9500);
    }

    @Test
    @DisplayName("updateSchedule - 정비 주기 없음")
    void updateSchedule_NotFound() {
        MaintenanceScheduleUpdateRequest request = new MaintenanceScheduleUpdateRequest(
                10000, 12, null, null
        );

        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.updateSchedule(scheduleId, request, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("updateSchedule - 다른 사용자의 정비 주기")
    void updateSchedule_AccessDenied() {
        MaintenanceScheduleUpdateRequest request = new MaintenanceScheduleUpdateRequest(
                10000, 12, null, null
        );

        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> scheduleService.updateSchedule(scheduleId, request, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_SCHEDULE_ACCESS_DENIED);
    }

    // ============ deleteSchedule ============

    @Test
    @DisplayName("deleteSchedule - 성공")
    void deleteSchedule_Success() {
        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.of(testSchedule));

        scheduleService.deleteSchedule(scheduleId, userId);

        assertThat(testSchedule.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteSchedule - 정비 주기 없음")
    void deleteSchedule_NotFound() {
        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.deleteSchedule(scheduleId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteSchedule - 다른 사용자의 정비 주기")
    void deleteSchedule_AccessDenied() {
        when(scheduleRepository.findByIdAndDeletedAtIsNull(scheduleId))
                .thenReturn(Optional.of(testSchedule));

        assertThatThrownBy(() -> scheduleService.deleteSchedule(scheduleId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_SCHEDULE_ACCESS_DENIED);
    }
}
