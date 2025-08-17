package com.example.foodmap.place;

import com.example.foodmap.external.kakao.KakaoLocalClient;
import com.example.foodmap.place.dto.Place;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PlaceSearchService {
    private static final String DEFAULT_CATEGORY = "FD6"; // 카테고리코드(음식점)
    private final KakaoLocalClient kakao;

    public PlaceSearchService(KakaoLocalClient kakao) {
        this.kakao = kakao;
    }

    // 주소/좌표 기반 음식점 검색
    // - 주소면 좌표변환, 좌표 있으면 바로 목록 검색
    public List<Place> findByLocation(String address,
                                      Double x, Double y,
                                      Integer radius, Integer size, Integer page,
                                      String categoryGroupCode) {
        int r = (radius == null) ? 2000 : radius;
        int s = (size == null) ? 15 : size;
        int p = (page == null) ? 1 : page;
        String cg = (categoryGroupCode == null || categoryGroupCode.isBlank()) ? DEFAULT_CATEGORY : categoryGroupCode;

        double lon;
        double lat;

        if (address != null && !address.isBlank()) {
            var opt = kakao.geocodeAddress(address);
            if (opt.isEmpty()) return List.of();
            lat = opt.get()[0];
            lon = opt.get()[1];
        } else {
            if (x == null || y == null) {
                throw new IllegalArgumentException("Either address or both x,y must be provided.");
            }
            lon = x;
            lat = y;
        }

        return kakao.searchCategory(cg, lon, lat, r, s, p);
    }
}
