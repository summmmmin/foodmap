package com.example.foodmap.bookmark.service;

import com.example.foodmap.bookmark.dto.BookmarkView;
import com.example.foodmap.bookmark.repo.BookmarkQueryRepository;
import com.example.foodmap.bookmark.repo.BookmarkRepository;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.repo.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookmarkServiceTest {

    PlaceRepository placeRepository;
    BookmarkRepository bookmarkRepository;
    BookmarkQueryRepository bookmarkQueryRepository;
    BookmarkService service;

    @BeforeEach
    void setUp() {
        placeRepository = mock(PlaceRepository.class);
        bookmarkRepository = mock(BookmarkRepository.class);
        bookmarkQueryRepository = mock(BookmarkQueryRepository.class);
        service = new BookmarkService(placeRepository, bookmarkRepository, bookmarkQueryRepository);
    }

    @Test
    void addFromExternal_shouldUpsertPlace_thenUpsertBookmark() {
        // given
        Place place = new Place("kakao-1","식당","FD6","음식점","한식","주소",37.0,127.0);
        when(placeRepository.findIdByKakaoPlaceId("kakao-1")).thenReturn(10L);

        // when
        service.addFromExternal(1L, place, "메모");

        // then
        verify(placeRepository).upsertFromExternal(place);
        verify(placeRepository).findIdByKakaoPlaceId("kakao-1");
        verify(bookmarkRepository).upsert(1L, 10L, "메모");
    }

    @Test
    void getBookmarkView_notFound_shouldThrow() {
        when(bookmarkQueryRepository.findViewByBookmarkId(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.getBookmarkView(999L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Bookmark not found");
    }

    @Test
    void listByUserId_shouldDelegate() {
        when(bookmarkQueryRepository.findViewsByUserId(1L))
                .thenReturn(List.of(mock(BookmarkView.class)));
        assertThat(service.listByUserId(1L)).hasSize(1);
    }

    @Test
    void deleteByUserAndPlace_shouldDelegate() {
        service.deleteByUserAndPlace(1L, 10L);
        verify(bookmarkRepository).deleteByUserAndPlace(1L, 10L);
    }
}
