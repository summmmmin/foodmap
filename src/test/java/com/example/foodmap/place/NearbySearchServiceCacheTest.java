package com.example.foodmap.place;

import com.example.foodmap.external.kakao.KakaoLocalClient;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.service.NearbySearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
class NearbySearchServiceCacheTest {

    @Autowired
    NearbySearchService nearbySearchService;

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
    void nearbySearch_shouldUseCacheOnSecondCall() {
        var expected = List.of(new Place("K1","김밥천국","FD6","음식점","한식>분식","서울 강남구 ...",37.501,127.001));
        when(kakaoLocalClient.searchCategory(anyString(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt()))
                .thenReturn(expected);

        // 카카오 API 호출
        var first = nearbySearchService.findNearby("FD6", 127.027, 37.498, 2000, 15, 1);
        assertEquals(1, first.size());

        // 같은 파라미터 → 캐시
        var second = nearbySearchService.findNearby("FD6", 127.027, 37.498, 2000, 15, 1);
        assertEquals(1, second.size());

        verify(kakaoLocalClient, times(1))
                .searchCategory(anyString(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt());
    }
}
