package com.example.foodmap.bookmark.repo;

import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.repo.PlaceRepository;
import com.example.foodmap.config.MySqlContainerConfig;
import com.example.foodmap.place.repo.PlaceUpsertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class BookmarkRepositoryTest extends MySqlContainerConfig{

    @Resource
    PlaceRepository placeRepository;
    @Resource
    PlaceUpsertRepository placeUpsertRepository;
    @Resource
    BookmarkRepository bookmarkRepository;
    @Resource
    BookmarkUpsertRepository bookmarkUpsertRepository;
    @Resource
    BookmarkQueryRepository bookmarkQueryRepository;

    @Test
    void upsert_and_query_and_delete() {
        // given: place 생성
        Place p = new Place("kakao-999","테스트맛집","FD6","음식점","한식", "서울", 37.1, 127.2);
        placeUpsertRepository.upsertFromExternal(p);
        Long placeId = placeRepository.findIdByKakaoPlaceIdOrNull("kakao-999");
        long userId = 1L;

        // when: 등록
        int insertCount = bookmarkUpsertRepository.upsert(userId, placeId, "메모1");
        assertThat(insertCount).isGreaterThanOrEqualTo(1);

        // when: 같은거등록
        int upsertAgain = bookmarkUpsertRepository.upsert(userId, placeId, "메모2");
        assertThat(upsertAgain).isGreaterThanOrEqualTo(1);

        // then: 목록, 단건조회 확인
        var list = bookmarkQueryRepository.findViewsByUserId(userId)
                .stream()
                .filter(v -> "kakao-999".equals(v.kakaoPlaceId()))
                .toList();

        assertThat(list).hasSize(1);
        var view = list.getFirst();
        assertThat(view.memo()).isEqualTo("메모2");

        var found = bookmarkQueryRepository.findViewByBookmarkId(view.bookmarkId());
        assertThat(found).isNotNull();
        assertThat(found.kakaoPlaceId()).isEqualTo("kakao-999");

        bookmarkRepository.deleteByUserIdAndPlaceId(userId, placeId);
        boolean exists = bookmarkRepository.existsByUserIdAndPlaceId(userId, placeId);
        assertThat(exists).isFalse();

        var after = bookmarkQueryRepository.findViewsByUserId(userId);
        assertThat(after.stream().noneMatch(v -> "kakao-999".equals(v.kakaoPlaceId()))).isTrue();
    }
}
