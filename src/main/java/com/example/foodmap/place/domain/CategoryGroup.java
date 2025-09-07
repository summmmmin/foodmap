package com.example.foodmap.place.domain;

import org.springframework.util.StringUtils;

public enum CategoryGroup {
    FOOD("FD6"),   // 음식점
    CAFE("CE7");   // 카페

    private final String code;
    CategoryGroup(String code) { this.code = code; }
    public String code() { return code; }

    // 값이 비었으면 FD6 기본값
    public static String orDefault(String code) {
        return StringUtils.hasText(code) ? code : FOOD.code;
    }
}
