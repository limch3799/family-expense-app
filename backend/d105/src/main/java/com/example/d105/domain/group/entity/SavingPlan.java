package com.example.d105.domain.group.entity;
import com.example.d105.domain.group.entity.Group;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "saving_plans", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "plan_title", nullable = false, length = 50)
    private String planTitle;

    @Column(name = "account_no", nullable = false, length = 50)
    private String accountNo;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", updatable = false)
    private Group group;

    @Column(name = "target_amount", nullable = false)
    private Integer targetAmount;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    //진행중, 완료, 실패
    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false, length = 50)
    private String createdAt;

    @Column(name = "completed_at", length = 50)
    private String completedAt;
    @PrePersist
    public void prePersist() {
       this.createdAt  = LocalDateTime.now().toString();

    }

}
