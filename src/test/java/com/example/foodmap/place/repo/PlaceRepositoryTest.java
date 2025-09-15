package com.example.foodmap.place.repo;

import com.example.foodmap.place.dto.Place;
import com.example.foodmap.config.MySqlContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(MySqlContainerConfig.class)
class PlaceRepositoryTest {

    @Resource
    PlaceRepository placeRepository;
    @Resource
    PlaceUpsertRepository placeUpsertRepository;
    @Resource
    JdbcTemplate jdbcTemplate;

    @Test
    void upsertFromExternal_then_findIdByKakaoPlaceId() {
        // given
        Place p = new Place(
                "kakao-123",
                "식당",
                "FD6",
                "음식점",
                "한식",
                "서울",
                37.5, 127.0
        );

        // when
        placeUpsertRepository.upsertFromExternal(p);
        Long id1 = placeRepository.findIdByKakaoPlaceIdOrNull("kakao-123");

        // then
        assertThat(id1).isNotNull();

        // when (UPSERT update path)
        Place p2 = new Place(
                "kakao-123",
                "식당(수정)",
                "FD6",
                "음식점",
                "한식",
                "서울",
                37.5001, 127.0001
        );
        placeUpsertRepository.upsertFromExternal(p2);
        Long id2 = placeRepository.findIdByKakaoPlaceIdOrNull("kakao-123");

        assertThat(id2).isEqualTo(id1);

        // 좌표/이름 수정 확인
        var name = jdbcTemplate.queryForObject("SELECT name FROM place WHERE id=?", String.class, id1);
        assertThat(name).isEqualTo("식당(수정)");
    }
}
