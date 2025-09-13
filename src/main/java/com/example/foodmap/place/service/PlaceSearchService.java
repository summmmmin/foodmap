package com.example.foodmap.place.service;

import com.example.foodmap.common.error.BusinessException;
import com.example.foodmap.common.error.ErrorCode;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.dto.PlaceSearchRequest;
import com.example.foodmap.place.domain.CategoryGroup;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class PlaceSearchService {
    private final GeocodeService geocodeService;
    private final NearbySearchService nearbySearchService;

    public PlaceSearchService(GeocodeService geocodeService,
                              NearbySearchService nearbySearchService) {
        this.geocodeService = geocodeService;
        this.nearbySearchService = nearbySearchService;
    }

    // 주소/좌표 기반 음식점 검색
    // - 주소면 좌표변환, 좌표 있으면 바로 목록 검색
    public List<Place> findByLocation(PlaceSearchRequest request) {
        final int radius = Optional.ofNullable(request.getRadius()).orElse(2000);
        final int size   = Optional.ofNullable(request.getSize()).orElse(15);
        final int page   = Optional.ofNullable(request.getPage()).orElse(1);
        final String categoryGroupCode = CategoryGroup.orDefault(request.getCategoryGroupCode());

        double longitude;
        double latitude;

        if (StringUtils.hasText(request.getAddress())) {
            var geocode = geocodeService.geocode(request.getAddress());
            if (geocode.isEmpty()) return List.of();
            latitude = geocode.get()[0];
            longitude = geocode.get()[1];
        } else {
            if (request.getLongitude() == null || request.getLatitude() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "주소 또는 좌표(x,y) 중 하나는 반드시 필요합니다.");
            }
            longitude = request.getLongitude();
            latitude = request.getLatitude();
        }

        return nearbySearchService.findNearby(categoryGroupCode, longitude, latitude, radius, size, page);
    }
}
