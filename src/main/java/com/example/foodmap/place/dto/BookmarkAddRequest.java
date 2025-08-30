package com.example.foodmap.place.dto;

public record BookmarkAddRequest(
        Place place,
        String memo
) {}
