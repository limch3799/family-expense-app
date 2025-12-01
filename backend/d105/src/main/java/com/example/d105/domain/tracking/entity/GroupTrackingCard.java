package com.example.d105.domain.tracking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_tracking_cards", schema = "d105",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "card_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTrackingCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Long trackingId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "is_connected", nullable = false)
    private Boolean isConnected = true;
}