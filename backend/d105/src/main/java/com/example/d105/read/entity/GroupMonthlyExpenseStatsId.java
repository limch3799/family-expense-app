package com.example.d105.read.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GroupMonthlyExpenseStatsId implements Serializable {
    private Long groupId;
    private String yearMonth;
    private Short categoryId;
}