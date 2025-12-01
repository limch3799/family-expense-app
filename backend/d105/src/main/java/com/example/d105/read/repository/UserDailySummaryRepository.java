package com.example.d105.read.repository;

import com.example.d105.read.entity.UserDailySummary;
import com.example.d105.read.entity.UserDailySummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserDailySummaryRepository extends JpaRepository<UserDailySummary, UserDailySummaryId> {

    /**
     * 특정 월의 모든 일별 요약 조회 (monthly-calendar API용)
     */
    List<UserDailySummary> findByUserIdAndYearMonthOrderBySummaryDateAsc(Long userId, String yearMonth);

    /**
     * 최근 N일간 일별 요약 조회 (recent-trend API용)
     */
    @Query("SELECT u FROM UserDailySummary u WHERE u.userId = :userId " +
            "AND u.summaryDate >= :startDate " +
            "ORDER BY u.summaryDate ASC")
    List<UserDailySummary> findRecentDailySummaries(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate
    );

    /**
     * 일별 요약 UPSERT (비동기 업데이트용)
     */
    @Modifying
    @Query(value = """
        INSERT INTO d105_read.user_daily_summary 
        (user_id, year_month, summary_date, total_amount, transaction_count, transaction_ids, last_updated) 
        VALUES (:userId, :yearMonth, :summaryDate, :totalAmount, :transactionCount, CAST(:transactionIds AS jsonb), now())
        ON CONFLICT (user_id, year_month, summary_date) 
        DO UPDATE SET 
            total_amount = EXCLUDED.total_amount,
            transaction_count = EXCLUDED.transaction_count,
            transaction_ids = EXCLUDED.transaction_ids,
            last_updated = now()
        """, nativeQuery = true)
    void upsertUserDailySummary(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("summaryDate") LocalDate summaryDate,
            @Param("totalAmount") Long totalAmount,
            @Param("transactionCount") Integer transactionCount,
            @Param("transactionIds") String transactionIds
    );

    // UserDailySummaryRepository.java에 추가
    @Query("SELECT uds FROM UserDailySummary uds WHERE uds.userId IN :userIds AND uds.summaryDate = :date")
    List<UserDailySummary> findByUserIdsAndDate(@Param("userIds") List<Long> userIds, @Param("date") LocalDate date);
}