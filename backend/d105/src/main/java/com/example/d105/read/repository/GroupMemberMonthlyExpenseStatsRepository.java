package com.example.d105.read.repository;

import com.example.d105.read.entity.GroupMemberMonthlyExpenseStats;
import com.example.d105.read.entity.GroupMemberMonthlyExpenseStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberMonthlyExpenseStatsRepository extends JpaRepository<GroupMemberMonthlyExpenseStats, GroupMemberMonthlyExpenseStatsId> {

    List<GroupMemberMonthlyExpenseStats> findByGroupIdAndYearMonth(Long groupId, String yearMonth);

    List<GroupMemberMonthlyExpenseStats> findByGroupIdAndYearMonthAndUserId(Long groupId, String yearMonth, Long userId);

    @Modifying
    @Query(value = """
        INSERT INTO d105_read.group_member_monthly_expense_stats 
        (group_id, user_id, year_month, category_id, category_name, expense_amount, transaction_count, expense_percentage, last_updated) 
        VALUES (:groupId, :userId, :yearMonth, :categoryId, :categoryName, :expenseAmount, :transactionCount, :expensePercentage, now())
        ON CONFLICT (group_id, user_id, year_month, category_id) 
        DO UPDATE SET 
            expense_amount = EXCLUDED.expense_amount,
            transaction_count = EXCLUDED.transaction_count,
            expense_percentage = EXCLUDED.expense_percentage,
            last_updated = now()
        """, nativeQuery = true)
    void upsertGroupMemberMonthlyExpenseStats(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("categoryId") Short categoryId,
            @Param("categoryName") String categoryName,
            @Param("expenseAmount") Long expenseAmount,
            @Param("transactionCount") Integer transactionCount,
            @Param("expensePercentage") Double expensePercentage
    );

    List<GroupMemberMonthlyExpenseStats> findByGroupIdAndYearMonthAndCategoryId(Long groupId, String yearMonth, Short categoryId);
}