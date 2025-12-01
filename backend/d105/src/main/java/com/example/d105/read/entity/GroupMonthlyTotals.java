package com.example.d105.read.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "group_monthly_totals", schema = "d105_read")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(GroupMonthlyTotalsId.class)
public class GroupMonthlyTotals {

    @Id
    @Column(name = "group_id")
    private Long groupId;

    @Id
    @Column(name = "year_month", length = 7)
    private String yearMonth;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "active_member_count", nullable = false)
    private Integer activeMemberCount;

    @Column(name = "avg_amount_per_member", nullable = false)
    private Long avgAmountPerMember;

    @Column(name = "last_updated", nullable = false)
    private ZonedDateTime lastUpdated;
}