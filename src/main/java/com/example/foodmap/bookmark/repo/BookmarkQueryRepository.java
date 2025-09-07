package com.example.foodmap.bookmark.repo;

import com.example.foodmap.bookmark.dto.BookmarkView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookmarkQueryRepository {
    private final JdbcTemplate jdbc;

    public BookmarkQueryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public BookmarkView findViewByBookmarkId(long bookmarkId) {
        return jdbc.query("""
            SELECT b.id AS bookmark_id, b.memo, DATE_FORMAT(b.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
                   p.id AS place_id, p.kakao_place_id, p.name,
                   p.category_group_code, p.category_group_name, p.category_name,
                   p.road_address, p.latitude, p.longitude
              FROM bookmark b
              JOIN place p ON p.id = b.place_id
             WHERE b.id=?
        """, resultSet -> resultSet.next()
                        ? new BookmarkView(
                        resultSet.getLong("bookmark_id"),
                        resultSet.getString("memo"),
                        resultSet.getLong("place_id"),
                        resultSet.getString("kakao_place_id"),
                        resultSet.getString("name"),
                        resultSet.getString("category_group_code"),
                        resultSet.getString("category_group_name"),
                        resultSet.getString("category_name"),
                        resultSet.getString("road_address"),
                        resultSet.getDouble("latitude"),
                        resultSet.getDouble("longitude"),
                        resultSet.getString("created_at")
                )
                        : null,
                bookmarkId
        );
    }

    public List<BookmarkView> findViewsByUserId(long userId) {
        return jdbc.query("""
            SELECT b.id AS bookmark_id, b.memo, DATE_FORMAT(b.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
                   p.id AS place_id, p.kakao_place_id, p.name,
                   p.category_group_code, p.category_group_name, p.category_name,
                   p.road_address, p.latitude, p.longitude
              FROM bookmark b
              JOIN place p ON p.id = b.place_id
             WHERE b.user_id=?
             ORDER BY b.created_at DESC, b.id DESC
        """, (resultSet, n) -> new BookmarkView(
                resultSet.getLong("bookmark_id"),
                resultSet.getString("memo"),
                resultSet.getLong("place_id"),
                resultSet.getString("kakao_place_id"),
                resultSet.getString("name"),
                resultSet.getString("category_group_code"),
                resultSet.getString("category_group_name"),
                resultSet.getString("category_name"),
                resultSet.getString("road_address"),
                resultSet.getDouble("latitude"),
                resultSet.getDouble("longitude"),
                resultSet.getString("created_at")
        ), userId);
    }
}
