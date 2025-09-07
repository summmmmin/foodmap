package com.example.foodmap.bookmark.repo;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BookmarkRepository {
    private final JdbcTemplate jdbc;

    public BookmarkRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** 같은거등록하면 메모만 update */
    public int upsert(long userId, long placeId, String memo) {
        return jdbc.update("""
            INSERT INTO bookmark (user_id, place_id, memo)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE memo = VALUES(memo)
        """, userId, placeId, memo);
    }

    public int deleteByUserAndPlace(long userId, long placeId) {
        return jdbc.update("DELETE FROM bookmark WHERE user_id=? AND place_id=?", userId, placeId);
    }

    public int deleteByBookmarkId(long bookmarkId) {
        return jdbc.update("DELETE FROM bookmark WHERE id=?", bookmarkId);
    }
}
