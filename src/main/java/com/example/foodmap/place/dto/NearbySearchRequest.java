package com.example.foodmap.place.dto;

public record NearbySearchRequest(
        String categoryGroupCode,
        double longitude,
        double latitude,
        int radiusMeters,
        int pageSize,
        int pageNumber
) {}
