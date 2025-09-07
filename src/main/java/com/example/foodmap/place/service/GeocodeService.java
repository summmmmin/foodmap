package com.example.foodmap.place.service;

import com.example.foodmap.external.kakao.KakaoLocalClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class GeocodeService {

    private final KakaoLocalClient kakao;

    public GeocodeService(KakaoLocalClient kakao) {
        this.kakao = kakao;
    }

    // 주소 -> 좌표 결과 캐시
    @Cacheable(cacheNames = "geocode", key = "#address",
            condition = "T(org.springframework.util.StringUtils).hasText(#address)"
            ,unless = "#result == null")
    public Optional<double[]> geocode(String address) {
        if (!StringUtils.hasText(address)) return Optional.empty();
        return kakao.geocodeAddress(address);
    }

}
