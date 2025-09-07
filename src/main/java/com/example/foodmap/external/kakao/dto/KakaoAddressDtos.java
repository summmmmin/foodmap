package com.example.foodmap.external.kakao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class KakaoAddressDtos {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(Integer total_count) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String address_name,
            String x, // 경도
            String y  // 위도
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressSearchResponse(
            Meta meta,
            List<Document> documents
    ) {}
}
