package com.example.foodmap.bookmark.repo;

import com.example.foodmap.bookmark.domain.BookmarkEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends ListCrudRepository<BookmarkEntity, Long> {

    // 유저별 목록
    List<BookmarkEntity> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    // 유니크 키(user_id, place_id) 조회
    Optional<BookmarkEntity> findByUserIdAndPlaceId(Long userId, Long placeId);

    // 존재 여부
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);

    // userId + placeId로 삭제
    void deleteByUserIdAndPlaceId(Long userId, Long placeId);
}
