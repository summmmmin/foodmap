package com.example.foodmap.place;

import com.example.foodmap.config.MySqlContainerConfig;
import com.example.foodmap.place.dto.TopBookmarkedPlaceView;
import com.example.foodmap.place.repo.TopBookmarkedPlaceQueryRepository;
import com.example.foodmap.place.service.TopBookmarkedPlaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = TopBookmarkedPlaceServiceCacheTest.Config.class)
class TopBookmarkedPlaceServiceCacheTest extends MySqlContainerConfig {

    @Configuration
    @EnableCaching
    static class Config {
        @Bean CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("placeTopBookmarked");
        }
        @Bean TopBookmarkedPlaceQueryRepository repo() {
            return mock(TopBookmarkedPlaceQueryRepository.class);
        }
        @Bean Clock clock() {
            return Clock.fixed(Instant.parse("2025-09-07T00:00:00Z"), ZoneOffset.UTC);
        }
        @Bean TopBookmarkedPlaceService service(TopBookmarkedPlaceQueryRepository repo, Clock clock) {
            return new TopBookmarkedPlaceService(repo, clock);
        }
    }

    @Autowired
    TopBookmarkedPlaceService service;
    @Autowired
    TopBookmarkedPlaceQueryRepository repo;

    @BeforeEach
    void setup() {
        when(repo.findTopBookmarkedPlaces(any(), any(), anyInt()))
                .thenReturn(List.of(new TopBookmarkedPlaceView(
                        1L, "K1", "집밥", "FD6", "음식점", "한식", "서울", 37.5, 127.0, 10
                )));
    }

    @Test
    void sameArgs_shouldUseCache() {
        service.getTopBookmarkedPlaces(10, 30, "FD6");
        service.getTopBookmarkedPlaces(10, 30, "FD6");

        verify(repo, times(1)).findTopBookmarkedPlaces(any(), eq("FD6"), eq(10));
    }

    @Test
    void differentArgs_shouldNotUseCache() {
        service.getTopBookmarkedPlaces(10, 30, "FD6");
        service.getTopBookmarkedPlaces(10, 90, "FD6");

        // 파라미터 다를때 두 번 호출
        verify(repo, times(2)).findTopBookmarkedPlaces(any(), eq("FD6"), eq(10));
    }
}