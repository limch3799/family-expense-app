package com.example.d105.read.repository;

import com.example.d105.read.entity.GroupMonthlyExpenseStats;
import com.example.d105.read.entity.GroupMonthlyExpenseStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMonthlyExpenseStatsRepository extends JpaRepository<GroupMonthlyExpenseStats, GroupMonthlyExpenseStatsId> {

    List<GroupMonthlyExpenseStats> findByGroupIdAndYearMonth(Long groupId, String yearMonth);

    List<GroupMonthlyExpenseStats> findByGroupIdAndYearMonthAndCategoryId(Long groupId, String yearMonth, Short categoryId);

    @Modifying
    @Query(value = """
        INSERT INTO d105_read.group_monthly_expense_stats 
        (group_id, year_month, category_id, category_name, total_expense, transaction_count, member_count, expense_percentage, last_updated) 
        VALUES (:groupId, :yearMonth, :categoryId, :categoryName, :totalExpense, :transactionCount, :memberCount, :expensePercentage, now())
        ON CONFLICT (group_id, year_month, category_id) 
        DO UPDATE SET 
            total_expense = EXCLUDED.total_expense,
            transaction_count = EXCLUDED.transaction_count,
            member_count = EXCLUDED.member_count,
            expense_percentage = EXCLUDED.expense_percentage,
            last_updated = now()
        """, nativeQuery = true)
    void upsertGroupMonthlyExpenseStats(
            @Param("groupId") Long groupId,
            @Param("yearMonth") String yearMonth,
            @Param("categoryId") Short categoryId,
            @Param("categoryName") String categoryName,
            @Param("totalExpense") Long totalExpense,
            @Param("transactionCount") Integer transactionCount,
            @Param("memberCount") Integer memberCount,
            @Param("expensePercentage") Double expensePercentage
    );
}