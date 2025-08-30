package com.example.foodmap.place.repo;

import com.example.foodmap.place.dto.Place;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PlaceRepository {
    private final JdbcTemplate jdbc;

    public PlaceRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Long findIdByKakaoPlaceId(String kakaoPlaceId) {
        return jdbc.query("""
                SELECT id FROM place WHERE kakao_place_id=?
            """, resultSet -> resultSet.next() ? resultSet.getLong(1) : null, kakaoPlaceId);
    }

    /** kakao_place_id로 UPSERT */
    public void upsertFromExternal(Place place) {
        jdbc.update("""
            INSERT INTO place (
                kakao_place_id, name,
                category_group_code, category_group_name, category_name,
                phone, address, road_address,
                longitude, latitude, location
            ) VALUES (
                ?, ?, ?, ?, ?,
                NULL, NULL, ?,
                ?, ?, ST_SRID(POINT(?, ?), 4326)
            )
            ON DUPLICATE KEY UPDATE
                name=VALUES(name),
                category_group_code=VALUES(category_group_code),
                category_group_name=VALUES(category_group_name),
                category_name=VALUES(category_name),
                road_address=VALUES(road_address),
                longitude=VALUES(longitude),
                latitude=VALUES(latitude),
                location=ST_SRID(POINT(VALUES(longitude), VALUES(latitude)), 4326),
                updated_at=CURRENT_TIMESTAMP
        """,
                place.kakaoPlaceId(), place.name(),
                place.categoryGroupCode(), place.categoryGroupName(), place.categoryName(),
                place.roadAddress(),
                place.longitude(), place.latitude(), /* POINT */ place.longitude(), place.latitude());
    }
}
