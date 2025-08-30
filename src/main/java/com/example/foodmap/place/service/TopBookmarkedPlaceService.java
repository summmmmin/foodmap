package com.example.foodmap.place.service;

import com.example.foodmap.place.dto.TopBookmarkedPlaceView;
import com.example.foodmap.place.repo.TopBookmarkedPlaceQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TopBookmarkedPlaceService {

    private final TopBookmarkedPlaceQueryRepository queryRepository;
    private final Clock clock; // 현재시각
    @Autowired
    public TopBookmarkedPlaceService(TopBookmarkedPlaceQueryRepository queryRepository) {
        this(queryRepository, Clock.systemUTC());
    }
    public TopBookmarkedPlaceService(TopBookmarkedPlaceQueryRepository queryRepository, Clock clock) {
        this.queryRepository = queryRepository;
        this.clock = clock;
    }

    /**
     * 사람들이 많이 저장한 장소 Top-N
     * @param limit               상위 개수 (기본 10)
     * @param daysFromNow         최근 N일만 집계(예: 90일) null이면 전체 기간.
     * @param categoryGroupCode   카테고리 그룹 코드(FD6 등) null이면 전체.
     */
    @Cacheable(
            cacheNames = "placeTopBookmarked",
            key = "new org.springframework.cache.interceptor.SimpleKey(#limit, #daysFromNow, #categoryGroupCode)"
    )
    public List<TopBookmarkedPlaceView> getTopBookmarkedPlaces(
            Integer limit,
            Integer daysFromNow,
            String categoryGroupCode
    ) {
        int topN = Optional.ofNullable(limit).orElse(10);
        Instant since = (daysFromNow == null) ? null : Instant.now(clock).minus(daysFromNow, ChronoUnit.DAYS);
        return queryRepository.findTopBookmarkedPlaces(since, categoryGroupCode, topN);
    }
}
