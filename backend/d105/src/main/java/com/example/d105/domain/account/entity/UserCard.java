package com.example.d105.domain.account.entity;
import com.example.d105.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "user_cards", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "card_no", unique = true, nullable = false, length = 20)
    private String cardNo;

    @Column(name = "card_company", nullable = false, length = 50)
    private String cardCompany;

    @Column(name = "card_expiry_date", nullable = false)
    private ZonedDateTime cardExpiryDate;

    @Column(name = "withdrawal_account_no", nullable = false, length = 50)
    private String withdrawalAccountNo;

    @Column(name = "loaded_at", nullable = false)
    private ZonedDateTime loadedAt;

    @Column(name = "last_transaction_sync")
    private ZonedDateTime lastTransactionSync;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive ;

    @Column(name = "cvc_encrypted", nullable = true, length = 255)
    private String cvcEncrypted;

}
