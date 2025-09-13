package com.example.foodmap.bookmark.service;

import com.example.foodmap.bookmark.dto.BookmarkView;
import com.example.foodmap.bookmark.repo.BookmarkQueryRepository;
import com.example.foodmap.bookmark.repo.BookmarkRepository;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.repo.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.foodmap.common.error.*;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookmarkService {

    private final PlaceRepository placeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkQueryRepository bookmarkQueryRepository;

    public BookmarkService(PlaceRepository placeRepository,
                           BookmarkRepository bookmarkRepository,
                           BookmarkQueryRepository bookmarkQueryRepository) {
        this.placeRepository = placeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.bookmarkQueryRepository = bookmarkQueryRepository;
    }

    /** 외부 검색 결과로 추가
     * - 장소 UPSERT → place_id 조회 → 북마크 UPSERT
     * */
    @Transactional
    public void addFromExternal(long userId, Place place, String memo) {
        placeRepository.upsertFromExternal(place);
        Long placeId = placeRepository.findIdByKakaoPlaceId(place.kakaoPlaceId());
        if (placeId == null) {
            throw new BusinessException(ErrorCode.PLACE_UPSERT_FAILED, "Place upsert failed. kakao_place_id=" + place.kakaoPlaceId());
        }
        bookmarkRepository.upsert(userId, placeId, memo);
    }

    /** 이미 있는 place_id로 추가 */
    @Transactional
    public void addByPlaceId(long userId, long placeId, String memo) {
        bookmarkRepository.upsert(userId, placeId, memo);
    }

    /** 북마크 단건 조회  */
    @Transactional(readOnly = true)
    public BookmarkView getBookmarkView(long bookmarkId) {
        BookmarkView view = bookmarkQueryRepository.findViewByBookmarkId(bookmarkId);
        if (view == null) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND, "Bookmark not found: id=" + bookmarkId);
        }
        return view;
    }

    /** 사용자별 북마크 목록 조회 */
    @Transactional(readOnly = true)
    public List<BookmarkView> listByUserId(long userId) {
        return bookmarkQueryRepository.findViewsByUserId(userId);
    }

    /** 삭제 */
    @Transactional
    public void deleteByUserAndPlace(long userId, long placeId) {
        bookmarkRepository.deleteByUserAndPlace(userId, placeId);
    }

    @Transactional
    public void deleteByBookmarkId(long bookmarkId) {
        int affected = bookmarkRepository.deleteByBookmarkId(bookmarkId);
        if (affected == 0) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND, "Bookmark not found: id=" + bookmarkId);
        }
    }
}
