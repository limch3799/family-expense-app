package com.example.d105.read.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_monthly_totals", schema = "d105_read")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserMonthlyTotalsId.class)
public class UserMonthlyTotals {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "year_month", length = 7)
    private String yearMonth;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "uncategorized_amount", nullable = false)
    private Long uncategorizedAmount;

    @Column(name = "last_updated", nullable = false)
    private ZonedDateTime lastUpdated;
}