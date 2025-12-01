package com.example.d105.read.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "user_daily_summary", schema = "d105_read")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserDailySummaryId.class)
public class UserDailySummary {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "year_month", length = 7)
    private String yearMonth;

    @Id
    @Column(name = "summary_date")
    private LocalDate summaryDate;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount;

    @Column(name = "transaction_ids", columnDefinition = "jsonb")
    private String transactionIds;

    @Column(name = "last_updated", nullable = false)
    private ZonedDateTime lastUpdated;
}