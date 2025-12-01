package com.example.d105.domain.transaction.repository;

import com.example.d105.domain.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.transactionUniqueNo = :uniqueNo")
    Optional<Transaction> findByTransactionUniqueNo(@Param("uniqueNo") String transactionUniqueNo);

    // 수정: t.user.userId → t.userId
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    Page<Transaction> findByUserIdAndDateRangeAndNotExcluded(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    // 수정: t.user.userId → t.userId
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
            "AND t.transactionDate = :date " +
            "ORDER BY t.transactionTime DESC")
    List<Transaction> findByUserIdAndDateAndNotExcluded(
            @Param("userId") Long userId,
            @Param("date") LocalDate date
    );

    // 수정: t.user.userId → t.userId
    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
            "AND FUNCTION('TO_CHAR', t.transactionDate, 'YYYY-MM') = :yearMonth " +
            "AND t.isExcluded = false")
    List<Transaction> findByUserIdAndYearMonthAndNotExcluded(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );

    // 거래내역 날짜 집합 조회 (캐시 업데이트용)
    @Query("SELECT DISTINCT t.transactionDate FROM Transaction t WHERE t.transactionId IN :transactionIds")
    Set<LocalDate> findDistinctDatesByTransactionIds(@Param("transactionIds") List<Long> transactionIds);

    @Query("SELECT MIN(t.transactionDate) FROM Transaction t WHERE t.userId = :userId")
    LocalDate findFirstTransactionDateByUserId(@Param("userId") Long userId);

    /**
     * 거래 ID 목록으로 기본 거래정보만 조회 (최적화된 쿼리)
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionId IN :transactionIds " +
            "ORDER BY t.transactionDate DESC, t.transactionTime DESC")
    List<Transaction> findBasicTransactionInfoByIds(@Param("transactionIds") List<Long> transactionIds);

    // N+1 문제 해결: 연결된 거래를 한 번에 조회
    @Query("""
    SELECT DISTINCT t FROM Transaction t 
    LEFT JOIN AccountTransaction at ON t.transactionId = at.transactionId
    LEFT JOIN MemberTrackingAccount mta ON at.accountId = mta.accountId
    LEFT JOIN CardTransaction ct ON t.transactionId = ct.transactionId  
    LEFT JOIN GroupTrackingCard gtc ON ct.cardId = gtc.cardId
    WHERE t.userId = :userId 
    AND FUNCTION('TO_CHAR', t.transactionDate, 'YYYY-MM') = :yearMonth
    AND t.isExcluded = false
    AND ((mta.memberId = :memberId AND mta.isConnected = true) 
         OR (gtc.memberId = :memberId AND gtc.isConnected = true))
    ORDER BY t.transactionDate DESC, t.transactionTime DESC
    """)
    List<Transaction> findConnectedTransactionsForMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth,
            @Param("memberId") Long memberId
    );

    @Query("""
    SELECT DISTINCT t FROM Transaction t 
    LEFT JOIN AccountTransaction at ON t.transactionId = at.transactionId
    LEFT JOIN MemberTrackingAccount mta ON at.accountId = mta.accountId
    LEFT JOIN CardTransaction ct ON t.transactionId = ct.transactionId  
    LEFT JOIN GroupTrackingCard gtc ON ct.cardId = gtc.cardId
    WHERE t.userId = :userId 
    AND t.transactionDate = :date
    AND t.isExcluded = false
    AND ((mta.memberId = :memberId AND mta.isConnected = true) 
         OR (gtc.memberId = :memberId AND gtc.isConnected = true))
    ORDER BY t.transactionTime DESC
    """)
    List<Transaction> findConnectedTransactionsForDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("memberId") Long memberId
    );

    @Query("""
        SELECT t.transactionId
        FROM Transaction t
        LEFT JOIN AccountTransaction at ON t.transactionId = at.transactionId
        LEFT JOIN MemberTrackingAccount mta ON at.accountId = mta.accountId
        LEFT JOIN CardTransaction ct ON t.transactionId = ct.transactionId
        LEFT JOIN GroupTrackingCard gtc ON ct.cardId = gtc.cardId
        WHERE t.userId = :userId
          AND t.transactionDate = :date
          AND (
               (mta.memberId = :memberId AND mta.isConnected = true)
            OR (gtc.memberId = :memberId AND gtc.isConnected = true)
          )
        ORDER BY t.transactionTime DESC
    """)
    List<Long> findConnectedTransactionIdsForDateIncludingExcluded(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("memberId") Long memberId
    );
}