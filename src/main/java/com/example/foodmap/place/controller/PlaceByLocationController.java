package com.example.foodmap.place.controller;

import com.example.foodmap.place.dto.PlaceSearchRequest;
import com.example.foodmap.place.service.PlaceSearchService;
import com.example.foodmap.place.dto.Place;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlaceByLocationController {

    private final PlaceSearchService service;

    public PlaceByLocationController(PlaceSearchService service) {
        this.service = service;
    }

    @Operation(
            summary = "주소 또는 좌표로 주변 음식점 검색",
            description = """
                    address를 주면 주소→좌표 변환 후 반경 검색.
                    x,y(경도,위도)를 주면 바로 반경 검색.
                    categoryGroupCode 기본값은 FD6(음식점)입니다.
                    """
    )
    @GetMapping("/api/places/by-location")
    public ResponseEntity<List<Place>> byLocation(
            @Parameter(description = "주소(주소 또는 x,y 중 하나는 필수)")
            @RequestParam(required = false) String address,
            @Parameter(description = "경도 (WGS84)")
            @RequestParam(required = false) Double x,
            @Parameter(description = "위도 (WGS84)")
            @RequestParam(required = false) Double y,
            @Parameter(description = "반경(m) 0~20000, 기본 2000")
            @RequestParam(required = false) Integer radius,
            @Parameter(description = "페이지 크기 1~45, 기본 15")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "페이지 1~45, 기본 1")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "카테고리 그룹 코드 (기본 FD6=음식점)")
            @RequestParam(required = false) String categoryGroupCode
    ) {
        if ((address == null || address.isBlank()) && (x == null || y == null)) {
            return ResponseEntity.badRequest().build();
        }
        PlaceSearchRequest req = PlaceSearchRequest.builder()
                .address(address)
                .longitude(x)
                .latitude(y)
                .radius(radius)
                .size(size)
                .page(page)
                .categoryGroupCode(categoryGroupCode)
                .build();

        List<Place> list = service.findByLocation(req);
        return ResponseEntity.ok(list);
    }
}
