package com.example.d105.read.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserDailySummaryId implements Serializable {
    private Long userId;
    private String yearMonth;
    private LocalDate summaryDate;
}