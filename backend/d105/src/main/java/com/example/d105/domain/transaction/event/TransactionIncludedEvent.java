package com.example.d105.domain.transaction.event;

import com.example.d105.domain.transaction.entity.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionIncludedEvent extends ApplicationEvent {
    private final Transaction transaction;
    private final Long userId;
    private final String yearMonth;

    public TransactionIncludedEvent(Object source, Transaction transaction, Long userId, String yearMonth) {
        super(source);
        this.transaction = transaction;
        this.userId = userId;
        this.yearMonth = yearMonth;
    }
}