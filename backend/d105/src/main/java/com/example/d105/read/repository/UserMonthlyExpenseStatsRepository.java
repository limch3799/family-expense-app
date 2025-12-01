package com.example.d105.read.repository;

import com.example.d105.read.entity.UserMonthlyExpenseStats;
import com.example.d105.read.entity.UserMonthlyExpenseStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMonthlyExpenseStatsRepository extends JpaRepository<UserMonthlyExpenseStats, UserMonthlyExpenseStatsId> {

    List<UserMonthlyExpenseStats> findByUserIdAndYearMonth(Long userId, String yearMonth);

    @Modifying
    @Query(value = """
        INSERT INTO d105_read.user_monthly_expense_stats 
        (user_id, year_month, category_id, category_name, expense_amount, transaction_count, expense_percentage, transaction_ids, last_updated) 
        VALUES (:userId, :yearMonth, :categoryId, :categoryName, :expenseAmount, :transactionCount, :expensePercentage, CAST(:transactionIds AS jsonb), now())
        ON CONFLICT (user_id, year_month, category_id) 
        DO UPDATE SET 
            expense_amount = EXCLUDED.expense_amount,
            transaction_count = EXCLUDED.transaction_count,
            expense_percentage = EXCLUDED.expense_percentage,
            transaction_ids = EXCLUDED.transaction_ids,
            last_updated = now()
        """, nativeQuery = true)
    void upsertUserMonthlyExpenseStats(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("categoryId") Short categoryId,
            @Param("categoryName") String categoryName,
            @Param("expenseAmount") Long expenseAmount,
            @Param("transactionCount") Integer transactionCount,
            @Param("expensePercentage") Double expensePercentage,
            @Param("transactionIds") String transactionIds
    );

    @Modifying
    @Query("DELETE FROM UserMonthlyExpenseStats u WHERE u.userId = :userId AND u.yearMonth = :yearMonth AND u.categoryId = :categoryId")
    void deleteByUserIdAndYearMonthAndCategoryId(@Param("userId") Long userId, @Param("yearMonth") String yearMonth, @Param("categoryId") Short categoryId);

    @Query("SELECT u FROM UserMonthlyExpenseStats u WHERE u.userId = :userId AND u.yearMonth = :yearMonth AND u.categoryId = :categoryId")
    List<UserMonthlyExpenseStats> findByUserIdAndYearMonthAndCategoryId(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("categoryId") Short categoryId);


    // 그룹 멤버들의 통계를 한 번에 조회 (N+1 방지)
    @Query("SELECT u FROM UserMonthlyExpenseStats u WHERE u.userId IN :userIds AND u.yearMonth = :yearMonth")
    List<UserMonthlyExpenseStats> findByUserIdsAndYearMonth(
            @Param("userIds") List<Long> userIds,
            @Param("yearMonth") String yearMonth
    );
}