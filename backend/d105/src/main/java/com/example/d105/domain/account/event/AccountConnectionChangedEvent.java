package com.example.d105.domain.account.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AccountConnectionChangedEvent extends ApplicationEvent {
    private final Long userId;
    private final Long accountId;
    private final String action; // "계좌 연결" 또는 "계좌 해제"

    public AccountConnectionChangedEvent(Object source, Long userId, Long accountId, String action) {
        super(source);
        this.userId = userId;
        this.accountId = accountId;
        this.action = action;
    }
}