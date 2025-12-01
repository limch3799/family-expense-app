package com.example.d105.domain.report.entity;
import com.example.d105.domain.group.entity.Group;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "ai_reports", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id",  updatable = false)
    private Group group;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "report_content", nullable = false, columnDefinition = "TEXT")
    private String reportContent;

    @Column(name = "generated_at", nullable = false)
    private ZonedDateTime generatedAt;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        // 문제 2: 시간대 지정해서 ZonedDateTime 생성
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        this.generatedAt = now;

    }
}
