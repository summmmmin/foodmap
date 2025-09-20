package com.example.foodmap.place.repo;

import com.example.foodmap.place.domain.PlaceEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface PlaceRepository extends ListCrudRepository<PlaceEntity, Long> {

    interface IdOnly { Long getId(); }

    Optional<IdOnly> findByKakaoPlaceId(String kakaoPlaceId);

    boolean existsByKakaoPlaceId(String kakaoPlaceId);

    default Long findIdByKakaoPlaceIdOrNull(String kakaoPlaceId) {
        return findByKakaoPlaceId(kakaoPlaceId).map(IdOnly::getId).orElse(null);
    }
}
