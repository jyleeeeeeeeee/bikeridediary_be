package com.bikeridediary.domain.place.repository;

import com.bikeridediary.domain.place.entity.PlaceChangeRequestEntity;
import com.bikeridediary.domain.place.entity.PlaceChangeRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlaceChangeRequestRepository extends JpaRepository<PlaceChangeRequestEntity, UUID> {

    // no(자동 증가 조회 번호)로 특정 request 조회 — DB 관리/디버깅용
    Optional<PlaceChangeRequestEntity> findByNo(Long no);

    // 어드민 목록: 상태별 페이징.
    // requester/targetPlace를 미리 fetch join → 응답 조립 시 lazy 배치 페치 회피
    // (Hibernate 6.5+ EntityBatchLoaderArrayParam이 UUID[] 바인딩 실패 이슈).
    @EntityGraph(attributePaths = {"requester", "targetPlace"})
    Page<PlaceChangeRequestEntity> findByStatus(
            PlaceChangeRequestStatus status, Pageable pageable);

    // 내 요청 페이징 (targetPlace만 fetch — requester는 본인이라 조회 자체 무의미)
    @EntityGraph(attributePaths = {"targetPlace"})
    Page<PlaceChangeRequestEntity> findByRequester_Id(
            UUID requesterId, Pageable pageable);

    // 특정 Place에 대한 PENDING UPDATE 요청 여부 (DB 중복 방지)
    boolean existsByTargetPlace_IdAndStatus(UUID targetPlaceId, PlaceChangeRequestStatus status);

    // 요청자 PENDING 개수 (CREATE 요청 상한 검증용)
    long countByRequester_IdAndStatus(UUID requesterId, PlaceChangeRequestStatus status);
}
