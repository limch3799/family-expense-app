package com.example.d105.domain.transaction.entity;
import com.example.d105.domain.account.entity.UserCard;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_transactions", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardTransaction {

    @Id
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "category_id",nullable = false)
    private Short categoryId;



}
