package com.example.d105.read.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "group_member_monthly_expense_stats", schema = "d105_read")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(GroupMemberMonthlyExpenseStatsId.class)
public class GroupMemberMonthlyExpenseStats {

    @Id
    @Column(name = "group_id")
    private Long groupId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "year_month", length = 7)
    private String yearMonth;

    @Id
    @Column(name = "category_id")
    private Short categoryId;

    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "expense_amount", nullable = false)
    private Long expenseAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "expense_percentage", nullable = false)
    private BigDecimal expensePercentage;

    @Column(name = "last_updated", nullable = false)
    private ZonedDateTime lastUpdated;
}