package com.example.foodmap.place.service;

import com.example.foodmap.common.CacheKeys;
import com.example.foodmap.external.kakao.KakaoLocalClient;
import com.example.foodmap.place.dto.Place;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NearbySearchService {

    private final KakaoLocalClient kakao;

    public NearbySearchService(KakaoLocalClient kakao) {
        this.kakao = kakao;
    }

    // 좌표 기반 주변 검색 캐시
    @Cacheable(
            cacheNames = "placeNearby",
            key = "T(com.example.foodmap.common.CacheKeys).nearKey(#x, #y, #radius, #size, #page, #categoryGroupCode)"
    )
    public List<Place> findNearby(String categoryGroupCode, double x, double y, int radius, int size, int page) {
        return kakao.searchCategory(categoryGroupCode, x, y, radius, size, page);
    }
}
