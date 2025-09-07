package com.example.foodmap.place.repo;

import com.example.foodmap.place.dto.TopBookmarkedPlaceView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class TopBookmarkedPlaceQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public TopBookmarkedPlaceQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /* 사람들이 많이 저장한 장소 Top-N.(기간, 카테고리, 몇개) */
    public List<TopBookmarkedPlaceView> findTopBookmarkedPlaces(
            Instant sinceCreatedAtUtc,
            String categoryGroupCode,
            int limit
    ) {
        String sql = """
            SELECT 
                p.id                           AS place_id,
                p.kakao_place_id               AS kakao_place_id,
                p.name                         AS name,
                p.category_group_code          AS category_group_code,
                p.category_group_name          AS category_group_name,
                p.category_name                AS category_name,
                p.road_address                 AS road_address,
                p.latitude                     AS latitude,
                p.longitude                    AS longitude,
                COUNT(*)                       AS bookmark_count
            FROM bookmark b
            JOIN place p ON p.id = b.place_id
            WHERE (? IS NULL OR b.created_at >= ?)
              AND (? IS NULL OR p.category_group_code = ?)
            GROUP BY 
                p.id, p.kakao_place_id, p.name, 
                p.category_group_code, p.category_group_name, p.category_name,
                p.road_address, p.latitude, p.longitude
            ORDER BY bookmark_count DESC, p.id DESC
            LIMIT ?
            """;

        Object since = (sinceCreatedAtUtc == null) ? null : java.sql.Timestamp.from(sinceCreatedAtUtc);
        Object cat   = (categoryGroupCode == null || categoryGroupCode.isBlank()) ? null : categoryGroupCode;

        return jdbcTemplate.query(sql, (rs, n) -> new TopBookmarkedPlaceView(
                rs.getLong("place_id"),
                rs.getString("kakao_place_id"),
                rs.getString("name"),
                rs.getString("category_group_code"),
                rs.getString("category_group_name"),
                rs.getString("category_name"),
                rs.getString("road_address"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getLong("bookmark_count")
        ), since, since, cat, cat, limit);
    }
}
