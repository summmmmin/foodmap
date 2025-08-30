package com.example.foodmap.user.dto;

import com.example.foodmap.user.domain.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String loginId,
        String username,
        String useYn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(), user.getLoginId(), user.getUsername(), user.getUseYn(),
                user.getCreatedAt(), user.getUpdatedAt()
        );
    }
}
