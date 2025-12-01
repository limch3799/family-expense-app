package com.example.d105.read.repository;

import com.example.d105.read.entity.GroupDailySummary;
import com.example.d105.read.entity.GroupDailySummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GroupDailySummaryRepository extends JpaRepository<GroupDailySummary, GroupDailySummaryId> {

    /**
     * 특정 월의 모든 일별 요약 조회 (monthly-calendar API용)
     */
    List<GroupDailySummary> findByGroupIdAndYearMonthOrderBySummaryDateAsc(Long groupId, String yearMonth);

    /**
     * 최근 N일간 일별 요약 조회 (recent-trend API용)
     */
    @Query("SELECT g FROM GroupDailySummary g WHERE g.groupId = :groupId " +
            "AND g.summaryDate >= :startDate " +
            "ORDER BY g.summaryDate ASC")
    List<GroupDailySummary> findRecentDailySummaries(
            @Param("groupId") Long groupId,
            @Param("startDate") LocalDate startDate
    );

    /**
     * 특정 날짜의 그룹 일별 요약 조회 (daily-transactions API용)
     */
    @Query("SELECT g FROM GroupDailySummary g WHERE g.groupId = :groupId " +
            "AND g.summaryDate = :summaryDate")
    List<GroupDailySummary> findByGroupIdAndSummaryDate(
            @Param("groupId") Long groupId,
            @Param("summaryDate") LocalDate summaryDate
    );

    /**
     * 그룹 일별 요약 UPSERT (비동기 업데이트용)
     */
    @Modifying
    @Query(value = """
        INSERT INTO d105_read.group_daily_summary 
        (group_id, year_month, summary_date, total_amount, transaction_count, last_updated) 
        VALUES (:groupId, :yearMonth, :summaryDate, :totalAmount, :transactionCount, now())
        ON CONFLICT (group_id, year_month, summary_date) 
        DO UPDATE SET 
            total_amount = EXCLUDED.total_amount,
            transaction_count = EXCLUDED.transaction_count,
            last_updated = now()
        """, nativeQuery = true)
    void upsertGroupDailySummary(
            @Param("groupId") Long groupId,
            @Param("yearMonth") String yearMonth,
            @Param("summaryDate") LocalDate summaryDate,
            @Param("totalAmount") Long totalAmount,
            @Param("transactionCount") Integer transactionCount
    );
}