package com.example.foodmap.place.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("place")
public record PlaceEntity(
        @Id Long id,
        String kakaoPlaceId,
        String name,
        String categoryGroupCode,
        String categoryGroupName,
        String categoryName,
        String roadAddress,
        Double latitude,
        Double longitude
) {}
