package com.example.foodmap.bookmark.dto;

public record BookmarkView(
        long bookmarkId,
        String memo,
        long placeId,
        String kakaoPlaceId,
        String name,
        String categoryGroupCode,
        String categoryGroupName,
        String categoryName,
        String roadAddress,
        double latitude,
        double longitude,
        String createdAt
) {}
