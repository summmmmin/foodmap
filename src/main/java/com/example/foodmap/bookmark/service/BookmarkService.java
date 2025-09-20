package com.example.foodmap.bookmark.service;

import com.example.foodmap.bookmark.domain.BookmarkEntity;
import com.example.foodmap.bookmark.dto.BookmarkView;
import com.example.foodmap.bookmark.repo.BookmarkQueryRepository;
import com.example.foodmap.bookmark.repo.BookmarkUpsertRepository;
import com.example.foodmap.bookmark.repo.BookmarkRepository;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.repo.PlaceUpsertRepository;
import com.example.foodmap.place.repo.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.foodmap.common.error.*;

import java.util.List;
import java.util.Optional;

@Service
public class BookmarkService {

    private final PlaceRepository placeRepository;
    private final PlaceUpsertRepository placeUpsertRepository;
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkQueryRepository bookmarkQueryRepository;
    private final BookmarkUpsertRepository bookmarkUpsertRepository;

    public BookmarkService(PlaceRepository placeRepository, PlaceUpsertRepository placeUpsertRepository,
                           BookmarkRepository bookmarkRepository, BookmarkQueryRepository bookmarkQueryRepository,
                           BookmarkUpsertRepository bookmarkUpsertRepository) {
        this.placeRepository = placeRepository;
        this.placeUpsertRepository = placeUpsertRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.bookmarkQueryRepository = bookmarkQueryRepository;
        this.bookmarkUpsertRepository = bookmarkUpsertRepository;
    }

    /** 외부 검색 결과로 추가
     * - 장소 UPSERT → place_id 조회 → 북마크 UPSERT
     * */
    @Transactional
    public void addFromExternal(long userId, Place place, String memo) {
        placeUpsertRepository.upsertFromExternal(place);
        Long placeId = placeRepository.findIdByKakaoPlaceIdOrNull(place.kakaoPlaceId());
        if (placeId == null) {
            throw new BusinessException(ErrorCode.PLACE_UPSERT_FAILED, "Place upsert failed. kakao_place_id=" + place.kakaoPlaceId());
        }
        bookmarkUpsertRepository.upsert(userId, placeId, memo);
    }

    /** 이미 있는 place_id로 추가 */
    @Transactional
    public void addByPlaceId(long userId, long placeId, String memo) {
        bookmarkUpsertRepository.upsert(userId, placeId, memo);
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
        bookmarkRepository.deleteByUserIdAndPlaceId(userId, placeId);
    }

    @Transactional
    public void deleteByBookmarkId(long bookmarkId) {
        if (!bookmarkRepository.existsById(bookmarkId)) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND,
                    "Bookmark not found: id=" + bookmarkId);
        }
        bookmarkRepository.deleteById(bookmarkId);
    }
}
