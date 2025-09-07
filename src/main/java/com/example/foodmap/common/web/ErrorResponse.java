package com.example.foodmap.common.web;

import com.example.foodmap.common.error.ErrorCode;
import lombok.Builder;

import java.time.Instant;

@Builder
public record ErrorResponse(
        String code,
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public static ErrorResponse of(ErrorCode ec, String message, String path) {
        return ErrorResponse.builder()
                .code(ec.getCode())
                .status(ec.getHttpStatus().value())
                .error(ec.getHttpStatus().getReasonPhrase())
                .message(message != null ? message : ec.getMessage())
                .path(path)
                .timestamp(Instant.now())
                .build();
    }
}
