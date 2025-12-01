package com.example.d105.domain.transaction.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryChartListRequest {
    private String yearMonth;
    private Short categoryId;
}