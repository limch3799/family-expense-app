package com.example.d105.domain.transaction.event;

import com.example.d105.domain.transaction.entity.Transaction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CategoryChangedEvent extends ApplicationEvent {
    private final Transaction transaction;
    private final Long userId;
    private final String yearMonth;
    private final Short oldCategoryId;
    private final Short newCategoryId;

    public CategoryChangedEvent(Object source, Transaction transaction, Long userId, String yearMonth,
                                Short oldCategoryId, Short newCategoryId) {
        super(source);
        this.transaction = transaction;
        this.userId = userId;
        this.yearMonth = yearMonth;
        this.oldCategoryId = oldCategoryId;
        this.newCategoryId = newCategoryId;
    }
}