package com.example.foodmap.bookmark.repo;

import com.example.foodmap.bookmark.dto.BookmarkView;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookmarkQueryRepository {

    private final JdbcClient jdbc;

    public BookmarkQueryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public BookmarkView findViewByBookmarkId(long bookmarkId) {
        String sql = """
            SELECT b.id AS bookmark_id,
                   b.memo,
                   DATE_FORMAT(b.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
                   p.id AS place_id,
                   p.kakao_place_id,
                   p.name,
                   p.category_group_code,
                   p.category_group_name,
                   p.category_name,
                   p.road_address,
                   p.latitude,
                   p.longitude
              FROM bookmark b
              JOIN place p ON p.id = b.place_id
             WHERE b.id = :bookmarkId
        """;

        return jdbc.sql(sql)
                .param("bookmarkId", bookmarkId)
                .query(BookmarkView.class)     // DataClassRowMapper가 record 생성자와 alias를 매핑
                .optional()                    // 0 or 1건
                .orElse(null);
    }

    public List<BookmarkView> findViewsByUserId(long userId) {
        String sql = """
            SELECT b.id AS bookmark_id,
                   b.memo,
                   DATE_FORMAT(b.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
                   p.id AS place_id,
                   p.kakao_place_id,
                   p.name,
                   p.category_group_code,
                   p.category_group_name,
                   p.category_name,
                   p.road_address,
                   p.latitude,
                   p.longitude
              FROM bookmark b
              JOIN place p ON p.id = b.place_id
             WHERE b.user_id = :userId
             ORDER BY b.created_at DESC, b.id DESC
        """;

        return jdbc.sql(sql)
                .param("userId", userId)
                .query(BookmarkView.class)
                .list();
    }
}
