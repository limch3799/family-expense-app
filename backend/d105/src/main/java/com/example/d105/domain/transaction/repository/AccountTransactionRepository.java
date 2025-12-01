package com.example.d105.domain.transaction.repository;

import com.example.d105.domain.transaction.entity.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    Optional<AccountTransaction> findByTransactionId(Long transactionId);

    // 배치 조회로 N+1 방지
    @Query("SELECT at FROM AccountTransaction at WHERE at.transactionId IN :transactionIds")
    List<AccountTransaction> findByTransactionIds(@Param("transactionIds") List<Long> transactionIds);

    // 지출 거래만 배치 조회 (account_transaction_type 기준)
    @Query("""
    SELECT at FROM AccountTransaction at 
    WHERE at.transactionId IN :transactionIds 
    AND at.accountTransactionType IN ('출금', '출금(이체)')
    """)
    List<AccountTransaction> findExpenseTransactionsByIds(@Param("transactionIds") List<Long> transactionIds);

}