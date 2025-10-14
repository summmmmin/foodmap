package com.example.foodmap.place;

import com.example.foodmap.config.MySqlContainerConfig;
import com.example.foodmap.external.kakao.KakaoLocalClient;
import com.example.foodmap.place.service.GeocodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class GeocodeServiceCacheTest extends MySqlContainerConfig {

    @Autowired
    GeocodeService geocodeService;

    @MockitoBean
    KakaoLocalClient kakaoLocalClient;

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    void flushRedis() {
        try (var conn = redisConnectionFactory.getConnection()) {
            conn.serverCommands().flushAll(); // 테스트 격리
        }
    }

    @Test
    void geocode_shouldUseCacheOnSecondCall() {
        when(kakaoLocalClient.geocodeAddress("서울 강남구")).thenReturn(Optional.of(new double[]{37.498, 127.027}));

        // 1.카카오 API 호출
        var first = geocodeService.geocode("서울 강남구");
        assertTrue(first.isPresent());

        // 2.캐시 (api호출x)
        var second = geocodeService.geocode("서울 강남구");
        assertTrue(second.isPresent());

        // 카카오 호출 1번만
        verify(kakaoLocalClient, times(1)).geocodeAddress("서울 강남구");
    }
}

