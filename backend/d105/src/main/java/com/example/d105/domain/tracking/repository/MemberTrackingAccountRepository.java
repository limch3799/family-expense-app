package com.example.d105.domain.tracking.repository;

import com.example.d105.domain.account.entity.UserAccount;
import com.example.d105.domain.tracking.entity.MemberTrackingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberTrackingAccountRepository extends JpaRepository<MemberTrackingAccount, Long> {

    @Query("SELECT mta FROM MemberTrackingAccount mta WHERE mta.memberId = :memberId AND mta.accountId = :accountId")
    Optional<MemberTrackingAccount> findByMemberIdAndAccountId(@Param("memberId") Long memberId, @Param("accountId") Long accountId);

    @Modifying
    @Query("UPDATE MemberTrackingAccount mta SET mta.isConnected = :isConnected WHERE mta.memberId = :memberId AND mta.accountId = :accountId")
    void updateConnectionStatus(@Param("memberId") Long memberId, @Param("accountId") Long accountId, @Param("isConnected") Boolean isConnected);

    // 단순화된 쿼리 - JOIN을 피하고 직접 조인
    @Query("SELECT mta FROM MemberTrackingAccount mta " +
            "WHERE mta.memberId IN (" +
            "   SELECT gm.memberId FROM GroupMember gm " +
            "   WHERE gm.group.groupId = :groupId AND gm.user.userId = :userId AND gm.exitedAt IS NULL" +
            ") AND mta.isConnected = true")
    List<MemberTrackingAccount> findConnectedAccountsByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    // 수정된 쿼리 - accountId만 반환
    @Query("SELECT mta FROM MemberTrackingAccount mta WHERE mta.memberId = :memberId AND mta.isConnected = true")
    List<MemberTrackingAccount> findConnectedAccountsByMemberId(@Param("memberId") Long memberId);
}