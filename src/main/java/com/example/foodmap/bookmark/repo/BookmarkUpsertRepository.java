package com.example.foodmap.bookmark.repo;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BookmarkUpsertRepository {
    private final JdbcClient jdbc;

    public BookmarkUpsertRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public int upsert(long userId, long placeId, String memo) {
        String sql = """
            INSERT INTO bookmark (user_id, place_id, memo)
            VALUES (:userId, :placeId, :memo)
            AS new
            ON DUPLICATE KEY UPDATE memo = new.memo
        """;
        return jdbc.sql(sql)
                .param("userId", userId)
                .param("placeId", placeId)
                .param("memo", memo)
                .update();
    }
}
