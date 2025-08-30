package com.example.foodmap.place.dto;

public record TopBookmarkedPlaceView(
        long placeId,
        String kakaoPlaceId,
        String name,
        String categoryGroupCode,
        String categoryGroupName,
        String categoryName,
        String roadAddress,
        double latitude,
        double longitude,
        long bookmarkCount
) {}
