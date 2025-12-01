package com.example.d105.domain.transaction.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class BatchTransactionSavedEvent extends ApplicationEvent {
    private final List<Long> transactionIds;
    private final Long userId;
    private final String yearMonth;

    public BatchTransactionSavedEvent(Object source, List<Long> transactionIds, Long userId, String yearMonth) {
        super(source);
        this.transactionIds = transactionIds;
        this.userId = userId;
        this.yearMonth = yearMonth;
    }
}
