package com.example.d105.domain.transaction.entity;

import com.example.d105.domain.account.entity.UserAccount;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_transactions", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransaction {

    @Id
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "account_transaction_type", nullable = false, length = 30)
    private String accountTransactionType;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "category_id",nullable = false)
    private Short categoryId;

}
