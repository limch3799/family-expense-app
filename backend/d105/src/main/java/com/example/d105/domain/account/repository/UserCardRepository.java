package com.example.d105.domain.account.repository;

import com.example.d105.domain.account.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardRepository extends JpaRepository<UserCard, Long> {

    @Query("SELECT uc FROM UserCard uc WHERE uc.userId = :userId AND uc.cardNo = :cardNo AND uc.isActive = true")
    Optional<UserCard> findByUserIdAndCardNoAndIsActiveTrue(@Param("userId") Long userId, @Param("cardNo") String cardNo);

    @Query("SELECT uc FROM UserCard uc WHERE uc.userId = :userId AND uc.isActive = true")
    List<UserCard> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);

}