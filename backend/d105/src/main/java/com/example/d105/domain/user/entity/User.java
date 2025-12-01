package com.example.d105.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="users", schema = "d105")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", unique = true, nullable = false, length = 30)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "simple_password", nullable = false, length = 200)
    private String simplePassword;

    @Column(name = "username", nullable = false, length = 200)
    private String username;

    @Column(name = "user_key", unique = true, nullable = false, length = 400)
    private String userKey;

    @Column(name = "phone_number", unique = true, nullable = false, length = 100)
    private String phoneNumber;

    @Column(name = "birth_date", nullable = false, length = 100)
    private String birthDate;

    @Column(name = "gender", nullable = false, length = 50)
    private String gender;

    @Column(name = "planer_push_enabled", nullable = false)
    private Boolean planerPushEnabled = true;

    @Column(name = "reporter_push_enabled", nullable = false)
    private Boolean reporterPushEnabled = true;

    @Column(name = "transaction_push_enabled", nullable = false)
    private Boolean transactionPushEnabled = true;

    @Column(name = "created_at", nullable = false, length = 40)
    private String createdAt;

    @Column(name = "updated_at", nullable = false, length = 40)
    private String updatedAt;

    @Column(name = "deleted_at", length = 40)
    private String deletedAt;


}
