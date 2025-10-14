package com.example.foodmap.batch.top;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TopBookmarkMaterializeService {

    private final JdbcTemplate jdbc;

    public record Scope(@Nullable String categoryGroupCode) {}
    public record RawAgg(long placeId, long cnt, int rankNo) {}
    public record SnapshotRow(LocalDate snapshotDate, int periodDays, @Nullable String categoryGroupCode,
                              long placeId, long cnt, int rankNo) {}

    public List<RawAgg> readAgg(LocalDate runDateKst, int periodDays, Scope scope, int topN, ZoneId zone) {
        LocalDate startDate = runDateKst.minusDays(periodDays - 1L);
        ZonedDateTime startZdt = startDate.atStartOfDay(zone);
        ZonedDateTime endZdt   = runDateKst.plusDays(1).atStartOfDay(zone);

        String base = """
            SELECT place_id, cnt,
                   ROW_NUMBER() OVER (ORDER BY cnt DESC, place_id ASC) AS rank_no
            FROM (
              SELECT b.place_id, COUNT(*) AS cnt
              FROM bookmark b
              JOIN place p ON p.id = b.place_id
              WHERE b.created_at >= ? AND b.created_at < ?
            """;

        String categoryFilter = (scope != null && scope.categoryGroupCode() != null)
                ? " AND p.category_group_code = ? "
                : "";

        String tail = """
              GROUP BY b.place_id
            ) t
            ORDER BY cnt DESC, place_id ASC
            LIMIT ?
        """;

        String sql = base + categoryFilter + tail;

        if (categoryFilter.isEmpty()) {
            return jdbc.query(sql, (rs, i) ->
                            new RawAgg(rs.getLong("place_id"), rs.getLong("cnt"), rs.getInt("rank_no")),
                    java.sql.Timestamp.from(startZdt.toInstant()),
                    java.sql.Timestamp.from(endZdt.toInstant()),
                    topN
            );
        } else {
            return jdbc.query(sql, (rs, i) ->
                            new RawAgg(rs.getLong("place_id"), rs.getLong("cnt"), rs.getInt("rank_no")),
                    java.sql.Timestamp.from(startZdt.toInstant()),
                    java.sql.Timestamp.from(endZdt.toInstant()),
                    scope.categoryGroupCode(),
                    topN
            );
        }
    }

    public List<SnapshotRow> process(LocalDate runDateKst, int periodDays, Scope scope, List<RawAgg> rows) {
        String category = scope == null ? null : scope.categoryGroupCode();
        return rows.stream()
                .map(r -> new SnapshotRow(runDateKst, periodDays, category, r.placeId(), r.cnt(), r.rankNo()))
                .toList();
    }

    @Transactional
    public int write(LocalDate runDateKst, int periodDays, Scope scope, List<SnapshotRow> rows) {
        String category = (scope == null) ? null : scope.categoryGroupCode();

        if (category == null) {
            jdbc.update("""
                DELETE FROM top_bookmarked_snapshot_hist
                 WHERE snapshot_date = ? AND period_days = ?
            """, Date.valueOf(runDateKst), periodDays);
        } else {
            jdbc.update("""
                DELETE FROM top_bookmarked_snapshot_hist
                 WHERE snapshot_date = ? AND period_days = ? AND category_group_code = ?
            """, Date.valueOf(runDateKst), periodDays, category);
        }

        if (rows.isEmpty()) return 0;

        jdbc.batchUpdate("""
            INSERT INTO top_bookmarked_snapshot_hist
              (snapshot_date, period_days, category_group_code, rank_no, place_id, bookmark_count)
            VALUES (?, ?, ?, ?, ?, ?)
        """, rows, 1000, (ps, r) -> {
            ps.setDate(1, Date.valueOf(r.snapshotDate()));
            ps.setInt(2, r.periodDays());
            if (r.categoryGroupCode() == null) ps.setNull(3, java.sql.Types.VARCHAR);
            else ps.setString(3, r.categoryGroupCode());
            ps.setInt(4, r.rankNo());
            ps.setLong(5, r.placeId());
            ps.setLong(6, r.cnt());
        });

        return rows.size();
    }
}
