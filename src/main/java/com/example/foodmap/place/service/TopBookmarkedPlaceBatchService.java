package com.example.foodmap.place.service;

import com.example.foodmap.place.dto.TopBookmarkedPlaceView;
import com.example.foodmap.place.repo.TopBookmarkedPlaceQueryRepository;
import com.example.foodmap.place.repo.TopBookmarkedSnapshotQueryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TopBookmarkedPlaceBatchService {

    private final TopBookmarkedSnapshotQueryRepository snapshotRepo;
    private final TopBookmarkedPlaceQueryRepository realtimeRepo;
    private final Clock clock;

    public TopBookmarkedPlaceBatchService(
            TopBookmarkedSnapshotQueryRepository snapshotRepo,
            TopBookmarkedPlaceQueryRepository realtimeRepo
    ) {
        this(snapshotRepo, realtimeRepo, Clock.system(ZoneId.of("Asia/Seoul")));
    }

    public TopBookmarkedPlaceBatchService(
            TopBookmarkedSnapshotQueryRepository snapshotRepo,
            TopBookmarkedPlaceQueryRepository realtimeRepo,
            Clock clock
    ) {
        this.snapshotRepo = snapshotRepo;
        this.realtimeRepo = realtimeRepo;
        this.clock = clock;
    }

    @Cacheable(
            cacheNames = "placeTopBookmarkedSnapshot",
            key = "new org.springframework.cache.interceptor.SimpleKey(#limit, #daysFromNow, #categoryGroupCode)"
    )
    public List<TopBookmarkedPlaceView> getTopBookmarkedPlaces(
            Integer limit,
            Integer daysFromNow,
            String categoryGroupCode
    ) {
        int topN = Optional.ofNullable(limit).orElse(10);
        int periodDays = Optional.ofNullable(daysFromNow).orElse(7);

        LocalDate snapshotDateKst = LocalDate.now(clock.getZone());

        List<TopBookmarkedPlaceView> fromSnapshot =
                snapshotRepo.findFromSnapshot(snapshotDateKst, periodDays, categoryGroupCode, topN);
        if (!fromSnapshot.isEmpty()) {
            return fromSnapshot;
        }

        Instant sinceUtc = Instant.now(clock).minus(periodDays, ChronoUnit.DAYS);
        return realtimeRepo.findTopBookmarkedPlaces(sinceUtc, categoryGroupCode, topN);
    }
}
