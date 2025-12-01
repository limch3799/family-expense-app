package com.example.d105.domain.tracking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_tracking_accounts", schema = "d105",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "account_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberTrackingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tracking_id")
    private Long trackingId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "is_connected", nullable = false)
    private Boolean isConnected = true;
}