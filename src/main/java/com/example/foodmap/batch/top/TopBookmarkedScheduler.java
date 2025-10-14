package com.example.foodmap.batch.top;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.ZoneId;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TopBookmarkedScheduler {

    private final JobLauncher jobLauncher;
    private final Job topBookmarkedSnapshotJob;

    private JobParameters base(LocalDate runDate, long periodDays, long topN, String categoryGroupCodeOrNull) {
        JobParametersBuilder b = new JobParametersBuilder()
                .addString("runDate", runDate.toString())
                .addString("zone", "Asia/Seoul")
                .addLong("periodDays", periodDays)
                .addLong("topN", topN);
        if (categoryGroupCodeOrNull != null && !categoryGroupCodeOrNull.isBlank()) {
            b.addString("categoryGroupCode", categoryGroupCodeOrNull);
        }
        return b.toJobParameters();
    }

    @Scheduled(cron = "0 00 3 * * *", zone = "Asia/Seoul")
    public void dailyCategoryFd6() throws Exception {
        LocalDate runDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        jobLauncher.run(topBookmarkedSnapshotJob, base(runDate, 7, 100, "FD6"));
        jobLauncher.run(topBookmarkedSnapshotJob, base(runDate, 30, 100, "FD6"));
        jobLauncher.run(topBookmarkedSnapshotJob, base(runDate, 90, 100, "FD6"));
    }

    @Scheduled(cron = "0 00 3 * * *", zone = "Asia/Seoul")
    public void dailyGlobal() throws Exception {
        LocalDate runDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        jobLauncher.run(topBookmarkedSnapshotJob, base(runDate, 7, 100, null));
        jobLauncher.run(topBookmarkedSnapshotJob, base(runDate, 30, 100, null));
        jobLauncher.run(topBookmarkedSnapshotJob, base(runDate, 90, 100, null));
    }
}
