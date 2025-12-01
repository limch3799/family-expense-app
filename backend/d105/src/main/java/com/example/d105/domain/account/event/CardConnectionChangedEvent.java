package com.example.d105.domain.account.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CardConnectionChangedEvent extends ApplicationEvent {
    private final Long userId;
    private final Long cardId;
    private final String action; // "카드 연결" 또는 "카드 해제"

    public CardConnectionChangedEvent(Object source, Long userId, Long cardId, String action) {
        super(source);
        this.userId = userId;
        this.cardId = cardId;
        this.action = action;
    }
}