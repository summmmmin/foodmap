package com.example.foodmap.place.repo;

import com.example.foodmap.place.dto.TopBookmarkedPlaceView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public class TopBookmarkedSnapshotQueryRepository {

    private final JdbcTemplate jdbc;

    public TopBookmarkedSnapshotQueryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TopBookmarkedPlaceView> findFromSnapshot(
            LocalDate snapshotDate,
            int periodDays,
            @Nullable String categoryGroupCode,
            int limit
    ) {
        String sql = """
            SELECT 
                p.id                  AS place_id,
                p.kakao_place_id      AS kakao_place_id,
                p.name                AS name,
                p.category_group_code AS category_group_code,
                p.category_group_name AS category_group_name,
                p.category_name       AS category_name,
                p.road_address        AS road_address,
                p.latitude            AS latitude,
                p.longitude           AS longitude,
                s.bookmark_count      AS bookmark_count
            FROM top_bookmarked_snapshot_hist s
            JOIN place p ON p.id = s.place_id
            WHERE s.snapshot_date = ?
              AND s.period_days = ?
              AND ( (? IS NULL AND s.category_group_code IS NULL) OR s.category_group_code = ? )
            ORDER BY s.rank_no ASC
            LIMIT ?
        """;

        return jdbc.query(sql, (rs, n) -> new TopBookmarkedPlaceView(
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
        ), Date.valueOf(snapshotDate), periodDays, categoryGroupCode, categoryGroupCode, limit);
    }
}
