package com.example.foodmap.place;

import com.example.foodmap.bookmark.service.BookmarkService;
import com.example.foodmap.config.MySqlContainerConfig;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.dto.TopBookmarkedPlaceView;
import com.example.foodmap.place.service.TopBookmarkedPlaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(MySqlContainerConfig.class)
class TopBookmarkedPlaceIntegrationTest {

    @Autowired BookmarkService bookmarkService;
    @Autowired TopBookmarkedPlaceService topService;
    @Autowired JdbcTemplate jdbc;
    @Autowired(required = false)
    CacheManager cacheManager;

    @BeforeEach
    void clean() {
        // DB
        jdbc.update("SET FOREIGN_KEY_CHECKS=0");
        jdbc.update("TRUNCATE TABLE bookmark");
        jdbc.update("TRUNCATE TABLE place");
        jdbc.update("SET FOREIGN_KEY_CHECKS=1");

        // 캐시
        if (cacheManager != null && cacheManager.getCache("placeTopBookmarked") != null) {
            cacheManager.getCache("placeTopBookmarked").clear();
        }
    }

    @Test
    void topBookmarked_countsAndOrder_viaServices() {
        // given: 같은 장소(K1)는 '서로 다른 사용자' 3명이 저장 → COUNT = 3
        //        다른 장소(K2)는 1명만 저장 → COUNT = 1
        Place k1 = new Place("K1", "식당A", "FD6", "음식점", "한식", "서울", 37.5, 127.0);
        Place k2 = new Place("K2", "식당B", "FD6", "음식점", "한식", "서울", 37.6, 127.1);

        // K1: user 1,2,3
        bookmarkService.addFromExternal(1L, k1, "m1");
        bookmarkService.addFromExternal(2L, k1, "m2");
        bookmarkService.addFromExternal(3L, k1, "m3");

        // K2: user 4
        bookmarkService.addFromExternal(4L, k2, "m4");

        // when
        List<TopBookmarkedPlaceView> list = topService.getTopBookmarkedPlaces(10, null, "FD6");

        // then: K1(3) 먼저, K2(1) 다음
        assertAll(
                () -> assertEquals(2, list.size()),
                () -> assertEquals("K1", list.get(0).kakaoPlaceId()),
                () -> assertEquals(3, list.get(0).bookmarkCount()),
                () -> assertEquals("K2", list.get(1).kakaoPlaceId()),
                () -> assertEquals(1, list.get(1).bookmarkCount())
        );
    }
}
