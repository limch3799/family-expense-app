package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class CategoryListResponse {
    private List<CategoryInfo> categories;

    @Getter
    @Builder
    public static class CategoryInfo {
        private Short categoryId;
        private String categoryName;
    }
}