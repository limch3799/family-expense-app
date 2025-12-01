package com.example.d105.read.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupDailySummaryId implements Serializable {
    private Long groupId;
    private String yearMonth;
    private LocalDate summaryDate;
}