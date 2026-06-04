package com.bikeridediary.domain.maintenance;

import com.bikeridediary.domain.bike.entity.BikeCategory;
import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.maintenance.dto.MaintenanceCreateRequest;
import com.bikeridediary.domain.maintenance.dto.MaintenanceResponse;
import com.bikeridediary.domain.maintenance.dto.MaintenanceUpdateRequest;
import com.bikeridediary.domain.maintenance.entity.MaintenanceEntity;
import com.bikeridediary.domain.maintenance.entity.MaintenanceType;
import com.bikeridediary.domain.maintenance.repository.MaintenanceRepository;
import com.bikeridediary.domain.maintenance.service.MaintenanceService;
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
@DisplayName("MaintenanceService 단위 테스트")
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private BikeRepository bikeRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    private UUID userId;
    private UUID otherUserId;
    private UUID bikeId;
    private UUID maintenanceId;
    private UserEntity testUser;
    private BikeEntity testBike;
    private MaintenanceEntity testMaintenance;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        bikeId = UUID.randomUUID();
        maintenanceId = UUID.randomUUID();

        testUser = UserEntity.create("kakao", "123456", "test@example.com", "테스트");
        setId(testUser, userId);

        testBike = BikeEntity.create(testUser, "Honda", "CB650R", 2024, BikeCategory.SPORT, 10000);
        setId(testBike, bikeId);

        testMaintenance = MaintenanceEntity.create(
                testBike, MaintenanceType.ENGINE_OIL, LocalDate.of(2026, 6, 1),
                5000, 50000, "엔진오일 교체", 10000, LocalDate.of(2026, 12, 1)
        );
        setId(testMaintenance, maintenanceId);
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

    // ============ getMaintenances ============

    @Test
    @DisplayName("getMaintenances - 성공")
    void getMaintenances_Success() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(maintenanceRepository.findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc(bikeId))
                .thenReturn(List.of(testMaintenance));

        List<MaintenanceResponse> result = maintenanceService.getMaintenances(bikeId, userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).maintenanceType()).isEqualTo(MaintenanceType.ENGINE_OIL);
        verify(maintenanceRepository).findByBikeEntityIdAndDeletedAtIsNullOrderByMaintenanceDateDesc(bikeId);
    }

    @Test
    @DisplayName("getMaintenances - 바이크 없음")
    void getMaintenances_BikeNotFound() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.getMaintenances(bikeId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_NOT_FOUND);
    }

    @Test
    @DisplayName("getMaintenances - 다른 사용자의 바이크")
    void getMaintenances_AccessDenied() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));

        assertThatThrownBy(() -> maintenanceService.getMaintenances(bikeId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_ACCESS_DENIED);
    }

    // ============ getMaintenance ============

    @Test
    @DisplayName("getMaintenance - 성공")
    void getMaintenance_Success() {
        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));

        MaintenanceResponse result = maintenanceService.getMaintenance(maintenanceId, userId);

        assertThat(result.id()).isEqualTo(maintenanceId);
        assertThat(result.maintenanceType()).isEqualTo(MaintenanceType.ENGINE_OIL);
        assertThat(result.cost()).isEqualTo(50000);
    }

    @Test
    @DisplayName("getMaintenance - 정비 기록 없음")
    void getMaintenance_NotFound() {
        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.getMaintenance(maintenanceId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("getMaintenance - 다른 사용자의 정비 기록 접근 거부")
    void getMaintenance_AccessDenied() {
        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));

        assertThatThrownBy(() -> maintenanceService.getMaintenance(maintenanceId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_ACCESS_DENIED);
    }

    // ============ createMaintenance ============

    @Test
    @DisplayName("createMaintenance - 성공")
    void createMaintenance_Success() {
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                bikeId, MaintenanceType.ENGINE_OIL, LocalDate.of(2026, 6, 1),
                5000, 50000, "엔진오일 교체", 10000, LocalDate.of(2026, 12, 1)
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(maintenanceRepository.save(any(MaintenanceEntity.class)))
                .thenReturn(testMaintenance);

        MaintenanceResponse result = maintenanceService.createMaintenance(request, userId);

        assertThat(result).isNotNull();
        assertThat(result.maintenanceType()).isEqualTo(MaintenanceType.ENGINE_OIL);
        verify(maintenanceRepository).save(any(MaintenanceEntity.class));
    }

    @Test
    @DisplayName("createMaintenance - 바이크 없음")
    void createMaintenance_BikeNotFound() {
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                bikeId, MaintenanceType.ENGINE_OIL, LocalDate.of(2026, 6, 1),
                5000, 50000, "엔진오일 교체", null, null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.createMaintenance(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_NOT_FOUND);

        verify(maintenanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMaintenance - 다른 사용자의 바이크")
    void createMaintenance_AccessDenied() {
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                bikeId, MaintenanceType.ENGINE_OIL, LocalDate.of(2026, 6, 1),
                5000, 50000, "엔진오일 교체", null, null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));

        assertThatThrownBy(() -> maintenanceService.createMaintenance(request, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_ACCESS_DENIED);

        verify(maintenanceRepository, never()).save(any());
    }

    // ============ updateMaintenance ============

    @Test
    @DisplayName("updateMaintenance - 성공")
    void updateMaintenance_Success() {
        MaintenanceUpdateRequest request = new MaintenanceUpdateRequest(
                MaintenanceType.FRONT_TIRE, LocalDate.of(2026, 7, 1),
                6000, 200000, "앞 타이어 교체", 16000, LocalDate.of(2027, 7, 1)
        );

        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));

        MaintenanceResponse result = maintenanceService.updateMaintenance(maintenanceId, request, userId);

        assertThat(result.maintenanceType()).isEqualTo(MaintenanceType.FRONT_TIRE);
        assertThat(result.cost()).isEqualTo(200000);
        assertThat(result.mileageAtMaintenance()).isEqualTo(6000);
    }

    @Test
    @DisplayName("updateMaintenance - 정비 기록 없음")
    void updateMaintenance_NotFound() {
        MaintenanceUpdateRequest request = new MaintenanceUpdateRequest(
                MaintenanceType.FRONT_TIRE, LocalDate.of(2026, 7, 1),
                6000, 200000, "앞 타이어 교체", null, null
        );

        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.updateMaintenance(maintenanceId, request, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_NOT_FOUND);
    }

    // ============ deleteMaintenance ============

    @Test
    @DisplayName("deleteMaintenance - 성공")
    void deleteMaintenance_Success() {
        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));

        maintenanceService.deleteMaintenance(maintenanceId, userId);

        assertThat(testMaintenance.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteMaintenance - 정비 기록 없음")
    void deleteMaintenance_NotFound() {
        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceService.deleteMaintenance(maintenanceId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteMaintenance - 다른 사용자의 정비 기록")
    void deleteMaintenance_AccessDenied() {
        when(maintenanceRepository.findByIdAndDeletedAtIsNull(maintenanceId))
                .thenReturn(Optional.of(testMaintenance));

        assertThatThrownBy(() -> maintenanceService.deleteMaintenance(maintenanceId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAINTENANCE_ACCESS_DENIED);
    }
}
