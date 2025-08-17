package com.example.foodmap.external.kakao.dto;

import java.util.List;

public class KakaoAddressDtos {
    public record Meta(Integer total_count) {}

    public record Document(
            String address_name,
            String x, // 경도
            String y  // 위도
    ) {}

    public record AddressSearchResponse(
            Meta meta,
            List<Document> documents
    ) {}
}
