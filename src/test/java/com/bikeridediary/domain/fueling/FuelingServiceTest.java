package com.bikeridediary.domain.fueling;


import com.bikeridediary.domain.bike.entity.BikeEntity;
import com.bikeridediary.domain.bike.repository.BikeRepository;
import com.bikeridediary.domain.fueling.dto.FuelingCreateRequest;
import com.bikeridediary.domain.fueling.dto.FuelingResponse;
import com.bikeridediary.domain.fueling.dto.FuelingStatsResponse;
import com.bikeridediary.domain.fueling.dto.FuelingUpdateRequest;
import com.bikeridediary.domain.fueling.entity.FuelType;
import com.bikeridediary.domain.fueling.entity.FuelingEntity;
import com.bikeridediary.domain.fueling.repository.FuelingRepository;
import com.bikeridediary.domain.fueling.service.FuelingService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FuelingService 단위 테스트")
class FuelingServiceTest {

    @Mock
    private FuelingRepository fuelingRepository;

    @Mock
    private BikeRepository bikeRepository;

    @InjectMocks
    private FuelingService fuelingService;

    private UUID userId;
    private UUID otherUserId;
    private UUID bikeId;
    private UUID fuelingId;
    private UserEntity testUser;
    private BikeEntity testBike;
    private FuelingEntity testFueling;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        bikeId = UUID.randomUUID();
        fuelingId = UUID.randomUUID();

        testUser = UserEntity.create("kakao", "123456", "test@example.com", "테스트");
        setId(testUser, userId);

        testBike = BikeEntity.create(testUser, "Honda", "CB650R", 2024, "Sport", 10000);
        setId(testBike, bikeId);

        testFueling = FuelingEntity.create(
                testBike, LocalDate.of(2026, 6, 15), 10000,
                new BigDecimal("12.50"), 1800, 22500,
                FuelType.PREMIUM, true, "테스트 주유", "GS칼텍스"
        );
        setId(testFueling, fuelingId);
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

    // ============ getFuelings ============

    @Test
    @DisplayName("getFuelings - 성공")
    void getFuelings_Success() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(fuelingRepository.findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(bikeId))
                .thenReturn(List.of(testFueling));

        List<FuelingResponse> result = fuelingService.getFuelings(bikeId, userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fuelType()).isEqualTo(FuelType.PREMIUM);
        assertThat(result.get(0).fuelAmount()).isEqualByComparingTo(new BigDecimal("12.50"));
    }

    @Test
    @DisplayName("getFuelings - 바이크 없음")
    void getFuelings_BikeNotFound() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fuelingService.getFuelings(bikeId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_NOT_FOUND);
    }

    @Test
    @DisplayName("getFuelings - 다른 사용자의 바이크")
    void getFuelings_AccessDenied() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));

        assertThatThrownBy(() -> fuelingService.getFuelings(bikeId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_ACCESS_DENIED);
    }

    // ============ getFueling ============

    @Test
    @DisplayName("getFueling - 성공")
    void getFueling_Success() {
        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.of(testFueling));

        FuelingResponse result = fuelingService.getFueling(fuelingId, userId);

        assertThat(result.id()).isEqualTo(fuelingId);
        assertThat(result.stationName()).isEqualTo("GS칼텍스");
    }

    @Test
    @DisplayName("getFueling - 주유 기록 없음")
    void getFueling_NotFound() {
        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fuelingService.getFueling(fuelingId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FUELING_NOT_FOUND);
    }

    @Test
    @DisplayName("getFueling - 다른 사용자의 주유 기록")
    void getFueling_AccessDenied() {
        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.of(testFueling));

        assertThatThrownBy(() -> fuelingService.getFueling(fuelingId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FUELING_ACCESS_DENIED);
    }

    // ============ createFueling ============

    @Test
    @DisplayName("createFueling - 성공 (만탱크, 연비 계산)")
    void createFueling_FullTank_Success() {
        FuelingCreateRequest request = new FuelingCreateRequest(
                bikeId, LocalDate.of(2026, 6, 15), 10500,
                new BigDecimal("12.00"), 1800, 21600,
                FuelType.PREMIUM, true, null, null
        );

        FuelingEntity prevFullTank = FuelingEntity.create(
                testBike, LocalDate.of(2026, 5, 1), 10000,
                new BigDecimal("10.00"), 1800, 18000,
                FuelType.PREMIUM, true, null, null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(fuelingRepository.findTopByBikeEntityIdAndIsFullTankTrueAndMileageAtFuelingLessThanAndDeletedAtIsNullOrderByMileageAtFuelingDesc(
                bikeId, 10500))
                .thenReturn(Optional.of(prevFullTank));
        when(fuelingRepository.sumFuelAmountBetweenMileage(bikeId, 10000, 10500))
                .thenReturn(new BigDecimal("12.00"));
        when(fuelingRepository.save(any(FuelingEntity.class)))
                .thenReturn(testFueling);

        FuelingResponse result = fuelingService.createFueling(request, userId);

        assertThat(result).isNotNull();
        verify(fuelingRepository).save(any(FuelingEntity.class));
    }

    @Test
    @DisplayName("createFueling - 성공 (만탱크 아님, 연비 미계산)")
    void createFueling_NotFullTank_Success() {
        FuelingCreateRequest request = new FuelingCreateRequest(
                bikeId, LocalDate.of(2026, 6, 15), 10500,
                new BigDecimal("5.00"), 1800, 9000,
                FuelType.REGULAR, false, "부분 주유", null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(fuelingRepository.save(any(FuelingEntity.class)))
                .thenReturn(testFueling);

        FuelingResponse result = fuelingService.createFueling(request, userId);

        assertThat(result).isNotNull();
        verify(fuelingRepository, never())
                .findTopByBikeEntityIdAndIsFullTankTrueAndMileageAtFuelingLessThanAndDeletedAtIsNullOrderByMileageAtFuelingDesc(any(), any());
    }

    @Test
    @DisplayName("createFueling - 바이크 없음")
    void createFueling_BikeNotFound() {
        FuelingCreateRequest request = new FuelingCreateRequest(
                bikeId, LocalDate.of(2026, 6, 15), 10500,
                new BigDecimal("12.00"), 1800, 21600,
                FuelType.PREMIUM, true, null, null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fuelingService.createFueling(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_NOT_FOUND);
        verify(fuelingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createFueling - 다른 사용자의 바이크")
    void createFueling_AccessDenied() {
        FuelingCreateRequest request = new FuelingCreateRequest(
                bikeId, LocalDate.of(2026, 6, 15), 10500,
                new BigDecimal("12.00"), 1800, 21600,
                FuelType.PREMIUM, true, null, null
        );

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));

        assertThatThrownBy(() -> fuelingService.createFueling(request, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BIKE_ACCESS_DENIED);
        verify(fuelingRepository, never()).save(any());
    }

    // ============ updateFueling ============

    @Test
    @DisplayName("updateFueling - 성공")
    void updateFueling_Success() {
        FuelingUpdateRequest request = new FuelingUpdateRequest(
                LocalDate.of(2026, 6, 20), 10600,
                new BigDecimal("15.00"), 1750, 26250,
                FuelType.REGULAR, false, "수정", "SK에너지"
        );

        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.of(testFueling));

        FuelingResponse result = fuelingService.updateFueling(fuelingId, request, userId);

        assertThat(result.fuelAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(result.stationName()).isEqualTo("SK에너지");
    }

    @Test
    @DisplayName("updateFueling - 주유 기록 없음")
    void updateFueling_NotFound() {
        FuelingUpdateRequest request = new FuelingUpdateRequest(
                LocalDate.of(2026, 6, 20), 10600,
                new BigDecimal("15.00"), 1750, 26250,
                FuelType.REGULAR, false, null, null
        );

        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fuelingService.updateFueling(fuelingId, request, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FUELING_NOT_FOUND);
    }

    @Test
    @DisplayName("updateFueling - 다른 사용자의 주유 기록")
    void updateFueling_AccessDenied() {
        FuelingUpdateRequest request = new FuelingUpdateRequest(
                LocalDate.of(2026, 6, 20), 10600,
                new BigDecimal("15.00"), 1750, 26250,
                FuelType.REGULAR, false, null, null
        );

        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.of(testFueling));

        assertThatThrownBy(() -> fuelingService.updateFueling(fuelingId, request, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FUELING_ACCESS_DENIED);
    }

    // ============ deleteFueling ============

    @Test
    @DisplayName("deleteFueling - 성공")
    void deleteFueling_Success() {
        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.of(testFueling));

        fuelingService.deleteFueling(fuelingId, userId);

        assertThat(testFueling.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteFueling - 주유 기록 없음")
    void deleteFueling_NotFound() {
        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> fuelingService.deleteFueling(fuelingId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FUELING_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteFueling - 다른 사용자의 주유 기록")
    void deleteFueling_AccessDenied() {
        when(fuelingRepository.findByIdAndDeletedAtIsNull(fuelingId))
                .thenReturn(Optional.of(testFueling));

        assertThatThrownBy(() -> fuelingService.deleteFueling(fuelingId, otherUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FUELING_ACCESS_DENIED);
    }

    // ============ getStats ============

    @Test
    @DisplayName("getStats - 성공 (기록 있음)")
    void getStats_Success() {
        testFueling.setFuelEfficiency(new BigDecimal("41.67"));

        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(fuelingRepository.findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(bikeId))
                .thenReturn(List.of(testFueling));

        FuelingStatsResponse result = fuelingService.getStats(bikeId, userId);

        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.totalFuelAmount()).isEqualByComparingTo(new BigDecimal("12.50"));
        assertThat(result.totalCost()).isEqualTo(22500L);
        assertThat(result.averageFuelEfficiency()).isEqualByComparingTo(new BigDecimal("41.67"));
    }

    @Test
    @DisplayName("getStats - 성공 (기록 없음)")
    void getStats_Empty() {
        when(bikeRepository.findByIdAndDeletedAtIsNull(bikeId))
                .thenReturn(Optional.of(testBike));
        when(fuelingRepository.findByBikeEntityIdAndDeletedAtIsNullOrderByFuelingDateDescMileageAtFuelingDesc(bikeId))
                .thenReturn(List.of());

        FuelingStatsResponse result = fuelingService.getStats(bikeId, userId);

        assertThat(result.totalCount()).isEqualTo(0);
        assertThat(result.averageFuelEfficiency()).isNull();
    }
}
