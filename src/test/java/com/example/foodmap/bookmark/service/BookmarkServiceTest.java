package com.example.foodmap.bookmark.service;

import com.example.foodmap.bookmark.domain.BookmarkEntity;
import com.example.foodmap.bookmark.dto.BookmarkView;
import com.example.foodmap.bookmark.repo.BookmarkQueryRepository;
import com.example.foodmap.bookmark.repo.BookmarkUpsertRepository;
import com.example.foodmap.bookmark.repo.BookmarkRepository;
import com.example.foodmap.common.error.BusinessException;
import com.example.foodmap.common.error.ErrorCode;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.repo.PlaceRepository;
import com.example.foodmap.place.repo.PlaceUpsertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookmarkServiceTest {

    PlaceRepository placeRepository;
    PlaceUpsertRepository placeUpsertRepository;
    BookmarkRepository bookmarkRepository;
    BookmarkQueryRepository bookmarkQueryRepository;
    BookmarkUpsertRepository bookmarkUpsertRepository;
    BookmarkService service;

    @BeforeEach
    void setUp() {
        placeRepository = mock(PlaceRepository.class);
        placeUpsertRepository = mock(PlaceUpsertRepository.class);
        bookmarkRepository = mock(BookmarkRepository.class);
        bookmarkQueryRepository = mock(BookmarkQueryRepository.class);
        bookmarkUpsertRepository = mock(BookmarkUpsertRepository.class);
        service = new BookmarkService(placeRepository, placeUpsertRepository, bookmarkRepository, bookmarkQueryRepository, bookmarkUpsertRepository);
    }

    @Test
    void addFromExternal_shouldUpsertPlace_thenUpsertBookmark() {
        // given
        Place place = new Place("kakao-1","식당","FD6","음식점","한식","주소",37.0,127.0);
        when(placeRepository.findIdByKakaoPlaceIdOrNull("kakao-1")).thenReturn(10L);

        // when
        service.addFromExternal(1L, place, "메모");

        // then
        verify(placeUpsertRepository).upsertFromExternal(place);
        verify(placeRepository).findIdByKakaoPlaceIdOrNull("kakao-1");
        verify(bookmarkUpsertRepository).upsert(1L, 10L, "메모");
    }

    @Test
    void getBookmarkView_notFound_shouldThrow() {
        when(bookmarkRepository.findById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.getBookmarkView(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.BOOKMARK_NOT_FOUND);
                });
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
        verify(bookmarkRepository).deleteByUserIdAndPlaceId(1L, 10L);
    }
}
