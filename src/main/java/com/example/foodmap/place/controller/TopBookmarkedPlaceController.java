package com.example.foodmap.place.controller;

import com.example.foodmap.place.dto.TopBookmarkedPlaceView;
import com.example.foodmap.place.service.TopBookmarkedPlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TopBookmarkedPlaceController {

    private final TopBookmarkedPlaceService service;

    public TopBookmarkedPlaceController(TopBookmarkedPlaceService service) {
        this.service = service;
    }

    @Operation(summary = "사람들이 많이 저장한 장소 Top-N")
    @GetMapping("/api/places/top-bookmarked")
    public ResponseEntity<List<TopBookmarkedPlaceView>> topBookmarked(
            @Parameter(description = "개수 (기본 10)") @RequestParam(required = false) Integer limit,
            @Parameter(description = "n일 (기본 전체 기간)") @RequestParam(required = false) Integer daysFromNow,
            @Parameter(description = "카테고리 그룹 코드 (기본 FD6음식점)") @RequestParam(required = false) String categoryGroupCode
    ) {
        return ResponseEntity.ok(service.getTopBookmarkedPlaces(limit, daysFromNow, categoryGroupCode));
    }
}
