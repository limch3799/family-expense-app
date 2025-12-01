package com.example.d105.domain.transaction.service;

import com.example.d105.domain.transaction.dto.request.MakeTransactionRequest;
import com.example.d105.domain.transaction.entity.AccountTransaction;
import com.example.d105.domain.transaction.entity.CardTransaction;
import com.example.d105.domain.transaction.entity.Transaction;
import com.example.d105.domain.transaction.event.BatchTransactionSavedEvent;
import com.example.d105.domain.transaction.repository.AccountTransactionRepository;
import com.example.d105.domain.transaction.repository.CardTransactionRepository;
import com.example.d105.domain.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MakeTransactionService {

    private final TransactionRepository transactionRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final ApplicationEventPublisher applicationEventPublisher; // ✅ 누락된 의존성 추가

    /**
     * 거래 추가 (카드/계좌 거래 모두 지원)
     */
    @Transactional
    public Long makeTransaction(Long userId, MakeTransactionRequest.CreateTransactionRequest request) {
        try {
            log.info("Creating transaction for userId={}, type={}, amount={}",
                    userId, request.getTransactionType(), request.getAmount());

            // 1. 메인 거래 엔티티 생성 및 저장
            Transaction transaction = createTransaction(userId, request);
            Transaction savedTransaction = transactionRepository.save(transaction);

            // 2. 거래 타입별 상세 정보 저장
            saveTransactionDetails(savedTransaction.getTransactionId(), request);

            // 3. 집계 업데이트를 위한 이벤트 발행
            publishTransactionSavedEvent(userId, savedTransaction, request);

            log.info("Transaction created successfully: transactionId={}", savedTransaction.getTransactionId());
            return savedTransaction.getTransactionId();

        } catch (Exception e) {
            log.error("Failed to create transaction for userId={}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("거래 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 메인 거래 엔티티 생성
     */
    private Transaction createTransaction(Long userId, MakeTransactionRequest.CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionUniqueNo(request.getTransactionUniqueNo());
        transaction.setTransactionDate(request.getTransactionDate());

        // 시간 처리 개선
        if (request.getTransactionDateTime() != null) {
            LocalTime localTime = request.getTransactionDateTime().toLocalTime();
            OffsetTime offsetTime = OffsetTime.of(localTime, ZoneOffset.of("+09:00"));
            transaction.setTransactionTime(offsetTime);
        }

        transaction.setTransactionType(request.getTransactionType());
        transaction.setAmount(request.getAmount());
        transaction.setIsExcluded(false); // 기본값: 포함

        return transaction;
    }

    /**
     * 거래 타입별 상세 정보 저장
     */
    private void saveTransactionDetails(Long transactionId, MakeTransactionRequest.CreateTransactionRequest request) {
        String transactionType = request.getTransactionType();

        if ("CARD".equals(transactionType)) {
            saveCardTransaction(transactionId, request);
        } else if ("ACCOUNT".equals(transactionType)) {
            saveAccountTransaction(transactionId, request);
        } else {
            log.warn("Unknown transaction type: {}", transactionType);
            throw new IllegalArgumentException("지원하지 않는 거래 타입입니다: " + transactionType);
        }
    }

    /**
     * 카드 거래 상세 정보 저장
     */
    private void saveCardTransaction(Long transactionId, MakeTransactionRequest.CreateTransactionRequest request) {
        CardTransaction cardTransaction = new CardTransaction();
        cardTransaction.setTransactionId(transactionId); // ✅ 오타 수정
        cardTransaction.setCardId(request.getId());
        cardTransaction.setMerchantName(request.getMerchantName());
        cardTransaction.setCategoryId(request.getCategoryId());

        cardTransactionRepository.save(cardTransaction);
        log.debug("Card transaction details saved: transactionId={}, cardId={}",
                transactionId, request.getId());
    }

    /**
     * 계좌 거래 상세 정보 저장
     */
    private void saveAccountTransaction(Long transactionId, MakeTransactionRequest.CreateTransactionRequest request) {
        AccountTransaction accountTransaction = new AccountTransaction();
        accountTransaction.setTransactionId(transactionId);
        accountTransaction.setAccountId(request.getId());
        // 계좌 거래 특화 필드들 추가 필요
        // accountTransaction.setDescription(request.getDescription());
        // accountTransaction.setCounterparty(request.getCounterparty());

        accountTransactionRepository.save(accountTransaction);
        log.debug("Account transaction details saved: transactionId={}, accountId={}",
                transactionId, request.getId());
    }

    /**
     * 집계 업데이트를 위한 이벤트 발행
     */
    private void publishTransactionSavedEvent(Long userId, Transaction savedTransaction,
                                              MakeTransactionRequest.CreateTransactionRequest request) {
        try {
            // ✅ 기존 BatchTransactionSavedEvent 구조에 맞춰서 생성
            BatchTransactionSavedEvent event = new BatchTransactionSavedEvent(
                    this, // source 객체
                    List.of(savedTransaction.getTransactionId()), // List<Long> transactionIds
                    userId, // Long userId
                    YearMonth.from(request.getTransactionDate()).toString() // String yearMonth (예: "2024-09")
            );

            applicationEventPublisher.publishEvent(event);
            log.debug("Transaction saved event published: userId={}, transactionId={}, yearMonth={}",
                    userId, savedTransaction.getTransactionId(), event.getYearMonth());

        } catch (Exception e) {
            log.error("Failed to publish transaction saved event: {}", e.getMessage(), e);
            // 이벤트 발행 실패해도 거래 생성은 성공으로 처리 (집계는 나중에 수동 복구 가능)
        }
    }


}