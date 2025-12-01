package com.example.d105.read.repository;

import com.example.d105.read.entity.UserMonthlyTotals;
import com.example.d105.read.entity.UserMonthlyTotalsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMonthlyTotalsRepository extends JpaRepository<UserMonthlyTotals, UserMonthlyTotalsId> {

    Optional<UserMonthlyTotals> findByUserIdAndYearMonth(Long userId, String yearMonth);

    @Modifying
    @Query(value = """
        INSERT INTO d105_read.user_monthly_totals 
        (user_id, year_month, total_amount, transaction_count, uncategorized_amount, last_updated) 
        VALUES (:userId, :yearMonth, :totalAmount, :transactionCount, :uncategorizedAmount, now())
        ON CONFLICT (user_id, year_month) 
        DO UPDATE SET 
            total_amount = EXCLUDED.total_amount,
            transaction_count = EXCLUDED.transaction_count,
            uncategorized_amount = EXCLUDED.uncategorized_amount,
            last_updated = now()
        """, nativeQuery = true)
    void upsertUserMonthlyTotals(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("totalAmount") Long totalAmount,
            @Param("transactionCount") Integer transactionCount,
            @Param("uncategorizedAmount") Long uncategorizedAmount
    );

    // updateGroupMonthlyTotals 최적화용
    @Query("SELECT u FROM UserMonthlyTotals u WHERE u.userId IN :userIds AND u.yearMonth = :yearMonth")
    List<UserMonthlyTotals> findByUserIdsAndYearMonth(@Param("userIds") List<Long> userIds, @Param("yearMonth") String yearMonth);
}