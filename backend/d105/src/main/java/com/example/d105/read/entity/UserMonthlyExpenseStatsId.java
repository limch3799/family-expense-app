package com.example.d105.read.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserMonthlyExpenseStatsId implements Serializable {
    private Long userId;
    private String yearMonth;
    private Short categoryId;
}