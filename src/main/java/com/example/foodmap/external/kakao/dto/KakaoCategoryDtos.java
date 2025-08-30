package com.example.foodmap.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class KakaoCategoryDtos {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            Integer total_count,
            Integer pageable_count,
            Boolean is_end
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String id,
            String place_name,
            String category_group_code,
            String category_group_name,
            String category_name,
            String road_address_name,
            String address_name,
            String x, // 경도
            String y  // 위도
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategorySearchResponse(
            Meta meta,
            List<Document> documents
    ) {}
}
