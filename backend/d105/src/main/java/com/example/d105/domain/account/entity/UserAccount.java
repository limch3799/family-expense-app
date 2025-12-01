package com.example.d105.domain.account.entity;


import com.example.d105.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
@Entity
@Table(name = "user_accounts", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "user_id")
    private Long userId;


    @Column(name = "account_no", unique = true, nullable = false, length = 50)
    private String accountNo;

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 50)
    private String bankName;

    @Column(name = "account_expiry_date", nullable = false)
    private ZonedDateTime accountExpiryDate;

    @Column(name = "loaded_at", nullable = false)
    private ZonedDateTime loadedAt;

    @Column(name = "last_transaction_sync")
    private ZonedDateTime lastTransactionSync;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive ;


}
