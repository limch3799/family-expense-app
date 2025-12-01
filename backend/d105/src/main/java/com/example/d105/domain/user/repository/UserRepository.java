package com.example.d105.domain.user.repository;

import com.example.d105.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends  JpaRepository<User, Long> {
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // 기존 UserRepository 인터페이스에 추가
    @Query(value = """
    SELECT DISTINCT u.user_id 
    FROM d105.users u 
    WHERE u.deleted_at IS NULL 
    AND EXISTS (
        SELECT 1 FROM d105.transactions t 
        WHERE t.user_id = u.user_id 
    )
    """, nativeQuery = true)
    List<Long> findActiveUserIds();

    // 추가할 메소드들
    boolean existsByEmailAndDeletedAtIsNull(String email);

    @Query("SELECT u.phoneNumber FROM User u WHERE u.deletedAt IS NULL AND u.phoneNumber IS NOT NULL")
    List<String> findAllPhoneNumbersByDeletedAtIsNull();

}
