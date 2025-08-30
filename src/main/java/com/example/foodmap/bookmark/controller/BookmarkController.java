package com.example.foodmap.bookmark.controller;

import com.example.foodmap.bookmark.dto.BookmarkView;
import com.example.foodmap.bookmark.service.BookmarkService;
import com.example.foodmap.place.dto.BookmarkAddRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService service;

    public BookmarkController(BookmarkService service) {
        this.service = service;
    }

    @Operation(summary = "북마크 추가")
    @PostMapping("/external")
    public ResponseEntity<Void> addFromExternal(
            @Parameter(description = "사용자 ID")
            @RequestHeader("X-USER-ID") long userId,
            @RequestBody BookmarkAddRequest body
    ) {
        service.addFromExternal(userId, body.place(), body.memo());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이미 존재하는 place_id로 북마크 추가")
    @PostMapping("/{placeId}")
    public ResponseEntity<Void> addByPlaceId(
            @RequestHeader("X-USER-ID") long userId,
            @PathVariable long placeId,
            @RequestParam(required = false) String memo
    ) {
        service.addByPlaceId(userId, placeId, memo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "북마크 id로 북마크 단건 조회")
    @GetMapping("/{bookmarkId}")
    public ResponseEntity<BookmarkView> getOne(@PathVariable long bookmarkId) {
        return ResponseEntity.ok(service.getBookmarkView(bookmarkId));
    }

    @Operation(summary = "사용자별 북마크 목록 조회")
    @GetMapping
    public ResponseEntity<List<BookmarkView>> listByUser(
            @RequestHeader("X-USER-ID") long userId
    ) {
        return ResponseEntity.ok(service.listByUserId(userId));
    }

    @Operation(summary = "사용자와 장소로 북마크 삭제")
    @DeleteMapping("/by-place/{placeId}")
    public ResponseEntity<Void> deleteByUserAndPlace(
            @RequestHeader("X-USER-ID") long userId,
            @PathVariable long placeId
    ) {
        service.deleteByUserAndPlace(userId, placeId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "북마크 id로 삭제")
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> deleteById(
            @PathVariable long bookmarkId
    ) {
        service.deleteByBookmarkId(bookmarkId);
        return ResponseEntity.noContent().build();
    }
}
