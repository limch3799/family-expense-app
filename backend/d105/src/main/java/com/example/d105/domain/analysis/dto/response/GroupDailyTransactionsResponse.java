package com.example.d105.domain.analysis.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class GroupDailyTransactionsResponse {
    private String date;
    private Long groupId;
    private Long totalAmount;
    private Integer totalCount;
    private List<GroupTransactionDetail> transactions;

    @Getter
    @Builder
    public static class GroupTransactionDetail {
        private Long transactionId;
        private String transactionTime;  // "12:30:00+09:00"
        private Long amount;             // 25000
        private String categoryName;     // "외식"
        private Boolean isExcluded;      // false
        private String description;      // "맥도날드 강남점"
        private String memberName;       // "김철수"
        private String realName;
    }
}