package com.example.foodmap.place.repo;

import com.example.foodmap.place.dto.Place;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class PlaceUpsertRepository {

    private final JdbcClient jdbcClient;

    public PlaceUpsertRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void upsertFromExternal(Place place) {
        String sql = """
            INSERT INTO place (
                kakao_place_id, name,
                category_group_code, category_group_name, category_name,
                phone, address, road_address,
                longitude, latitude, location
            ) VALUES (
                :kakaoPlaceId, :name,
                :categoryGroupCode, :categoryGroupName, :categoryName,
                NULL, NULL, :roadAddress,
                :longitude, :latitude, ST_SRID(POINT(:longitude, :latitude), 4326)
            )
            AS new
            ON DUPLICATE KEY UPDATE
                name                 = new.name,
                category_group_code  = new.category_group_code,
                category_group_name  = new.category_group_name,
                category_name        = new.category_name,
                road_address         = new.road_address,
                longitude            = new.longitude,
                latitude             = new.latitude,
                location             = ST_SRID(POINT(new.longitude, new.latitude), 4326),
                updated_at           = CURRENT_TIMESTAMP
            """;

        jdbcClient.sql(sql)
                .param("kakaoPlaceId",      place.kakaoPlaceId())
                .param("name",              place.name())
                .param("categoryGroupCode", place.categoryGroupCode())
                .param("categoryGroupName", place.categoryGroupName())
                .param("categoryName",      place.categoryName())
                .param("roadAddress",       place.roadAddress())
                .param("longitude",         place.longitude()) // POINT(lon, lat)!
                .param("latitude",          place.latitude())
                .update();
    }
}
