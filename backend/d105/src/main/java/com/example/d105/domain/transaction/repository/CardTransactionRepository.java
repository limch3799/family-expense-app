package com.example.d105.domain.transaction.repository;

import com.example.d105.domain.transaction.entity.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {

    Optional<CardTransaction> findByTransactionId(Long transactionId);

    // 배치 조회로 N+1 방지
    @Query("SELECT ct FROM CardTransaction ct WHERE ct.transactionId IN :transactionIds")
    List<CardTransaction> findByTransactionIds(@Param("transactionIds") List<Long> transactionIds);
}