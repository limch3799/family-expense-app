package com.example.d105.domain.group.entity;
import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "group_members", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id",  updatable = false)
    private Group group;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", updatable = false)
    private User user;

    @Column(name = "display_name", nullable = false, length = 30)
    private String displayName;

    @Column(name = "member_status", nullable = false)
    private Short memberStatus;

    @Column(name = "allowed_at", length = 30)
    private String allowedAt;

    @Column(name = "rejected_at", length = 30)
    private String rejectedAt;

    @Column(name = "exited_at", length = 30)
    private String exitedAt;

}
