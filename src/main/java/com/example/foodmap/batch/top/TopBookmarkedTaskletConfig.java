package com.example.foodmap.batch.top;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class TopBookmarkedTaskletConfig {

    private final TopBookmarkMaterializeService topBookmarkMaterializeService;

    @Bean
    public Job topBookmarkedSnapshotJob(JobRepository jobRepository, org.springframework.batch.core.Step materializeTopBookmarkedStep) {
        return new JobBuilder("TopBookmarkedSnapshotJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(materializeTopBookmarkedStep)
                .build();
    }

    @Bean
    public org.springframework.batch.core.Step materializeTopBookmarkedStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("materializeTopBookmarkedStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    JobParameters params = Objects.requireNonNull(
                            contribution.getStepExecution().getJobExecution().getJobParameters(),
                            "JobParameters must not be null"
                    );

                    String zoneText = Optional.ofNullable(params.getString("zone")).orElse("Asia/Seoul");
                    ZoneId zoneId = ZoneId.of(zoneText);

                    LocalDate runDate = Optional.ofNullable(params.getString("runDate"))
                            .map(LocalDate::parse)
                            .orElse(LocalDate.now(zoneId));

                    int periodDays = Optional.ofNullable(params.getLong("periodDays"))
                            .map(Long::intValue).orElse(7);
                    int topN = Optional.ofNullable(params.getLong("topN"))
                            .map(Long::intValue).orElse(50);

                    String categoryGroupCode = params.getString("categoryGroupCode");
                    var scope = new TopBookmarkMaterializeService.Scope(categoryGroupCode);

                    var aggregatedRows = topBookmarkMaterializeService.readAgg(runDate, periodDays, scope, topN, zoneId);
                    var snapshotRows   = topBookmarkMaterializeService.process(runDate, periodDays, scope, aggregatedRows);

                    topBookmarkMaterializeService.write(runDate, periodDays, scope, snapshotRows);
                    return org.springframework.batch.repeat.RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
