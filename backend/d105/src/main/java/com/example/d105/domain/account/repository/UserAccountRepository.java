package com.example.d105.domain.account.repository;

import com.example.d105.domain.account.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    @Query("SELECT ua FROM UserAccount ua WHERE ua.userId = :userId AND ua.accountNo = :accountNo AND ua.isActive = true")
    Optional<UserAccount> findByUserIdAndAccountNoAndIsActiveTrue(@Param("userId") Long userId, @Param("accountNo") String accountNo);

    @Query("SELECT ua FROM UserAccount ua WHERE ua.userId = :userId AND ua.isActive = true")
    List<UserAccount> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);


}