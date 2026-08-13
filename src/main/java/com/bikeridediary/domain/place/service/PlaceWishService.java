package com.bikeridediary.domain.place.service;

import com.bikeridediary.domain.place.dto.PlaceResponse;
import com.bikeridediary.domain.place.entity.PlaceEntity;
import com.bikeridediary.domain.place.entity.PlaceWishEntity;
import com.bikeridediary.domain.place.entity.PlaceWishId;
import com.bikeridediary.domain.place.repository.PlaceRepository;
import com.bikeridediary.domain.place.repository.PlaceWishRepository;
import com.bikeridediary.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.bikeridediary.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class PlaceWishService {

    private final PlaceRepository placeRepository;
    private final PlaceWishRepository placeWishRepository;


    @Transactional
    public PlaceResponse addWish(UUID placeId, UUID userId) {
        PlaceEntity place = placeRepository.findByIdAndDeletedAtIsNull(placeId)
                .orElseThrow(() -> new BusinessException(PLACE_NOT_FOUND));

        PlaceWishId id = new PlaceWishId(placeId, userId);
        if(!placeWishRepository.existsById(id)) {
            placeWishRepository.save(PlaceWishEntity.create(placeId, userId));
            place.incrementWishedCount();
        }

        return PlaceResponse.from(place, true);
    }

    @Transactional
    public PlaceResponse removeWish(UUID placeId, UUID userId) {
        PlaceEntity place = placeRepository.findByIdAndDeletedAtIsNull(placeId)
                .orElseThrow(() -> new BusinessException(PLACE_NOT_FOUND));

        PlaceWishId id = new PlaceWishId(placeId, userId);
        if(placeWishRepository.existsById(id)) {
            placeWishRepository.deleteById(id);
            place.decrementWishedCount();
        }

        return PlaceResponse.from(place, false);

    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> listMyWishes(UUID userId) {
        return placeWishRepository.findByIdUserIdWithPlace(userId).stream()
                .map(w -> PlaceResponse.from(w.getPlaceEntity(), true))
                .toList();

    }

}
