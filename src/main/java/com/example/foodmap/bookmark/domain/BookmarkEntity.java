package com.example.foodmap.bookmark.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("bookmark")
public record BookmarkEntity(
        @Id Long id,
        Long userId,
        Long placeId,
        String memo,
        LocalDateTime createdAt
) {}
