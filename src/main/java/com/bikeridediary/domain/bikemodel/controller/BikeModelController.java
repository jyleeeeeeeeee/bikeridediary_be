package com.bikeridediary.domain.bikemodel.controller;

import com.bikeridediary.domain.bikemodel.dto.BikeModelResponse;
import com.bikeridediary.domain.bikemodel.dto.ManufacturerResponse;
import com.bikeridediary.domain.bikemodel.service.BikeModelService;
import com.bikeridediary.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "BikeModel", description = "바이크 제조사/모델 마스터 데이터")
@RestController
@RequestMapping("/api/v1/bike-models")
@RequiredArgsConstructor
public class BikeModelController {

    private final BikeModelService bikeModelService;

    @Operation(summary = "제조사 목록 조회", description = "활성화된 제조사 목록을 정렬순서대로 반환")
    @GetMapping("/manufacturers")
    public ResponseEntity<ApiResponse<List<ManufacturerResponse>>> getManufacturers() {
        return ResponseEntity.ok(ApiResponse.ok(bikeModelService.getActiveManufacturers()));
    }

    @Operation(summary = "모델명 목록 조회", description = "특정 제조사의 고유 모델명 목록 반환")
    @GetMapping("/manufacturers/{manufacturerId}/models")
    public ResponseEntity<ApiResponse<List<String>>> getModelNames(
            @PathVariable Long manufacturerId) {
        return ResponseEntity.ok(ApiResponse.ok(bikeModelService.getModelNames(manufacturerId)));
    }

    @Operation(summary = "모델 상세 조회", description = "특정 제조사의 특정 모델에 대한 연식별 상세 정보 반환")
    @GetMapping("/manufacturers/{manufacturerId}/models/{modelName}")
    public ResponseEntity<ApiResponse<List<BikeModelResponse>>> getModelDetails(
            @PathVariable Long manufacturerId,
            @PathVariable String modelName) {
        return ResponseEntity.ok(ApiResponse.ok(bikeModelService.getModelDetails(manufacturerId, modelName)));
    }

    @Operation(summary = "특정 제조사 모델 동기화", description = "API-Ninjas에서 특정 제조사의 모델 데이터를 동기화")
    @PostMapping("/sync/{manufacturerId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> syncManufacturer(
            @PathVariable Long manufacturerId) {
        int count = bikeModelService.syncManufacturerModels(manufacturerId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("syncedCount", count)));
    }

    @Operation(summary = "전체 제조사 모델 동기화", description = "API-Ninjas에서 모든 활성 제조사의 모델 데이터를 동기화 (시간 소요)")
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> syncAll() {
        int count = bikeModelService.syncAllManufacturers();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("syncedCount", count)));
    }
}
