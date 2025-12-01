package com.example.d105.domain.tracking.repository;

import com.example.d105.domain.account.entity.UserCard;
import com.example.d105.domain.tracking.entity.GroupTrackingCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupTrackingCardRepository extends JpaRepository<GroupTrackingCard, Long> {

    @Query("SELECT gtc FROM GroupTrackingCard gtc WHERE gtc.memberId = :memberId AND gtc.cardId = :cardId")
    Optional<GroupTrackingCard> findByMemberIdAndCardId(@Param("memberId") Long memberId, @Param("cardId") Long cardId);

    @Modifying
    @Query("UPDATE GroupTrackingCard gtc SET gtc.isConnected = :isConnected WHERE gtc.memberId = :memberId AND gtc.cardId = :cardId")
    void updateConnectionStatus(@Param("memberId") Long memberId, @Param("cardId") Long cardId, @Param("isConnected") Boolean isConnected);

    // 단순화된 쿼리 - JOIN을 피하고 직접 조인
    @Query("SELECT gtc FROM GroupTrackingCard gtc " +
            "WHERE gtc.memberId IN (" +
            "   SELECT gm.memberId FROM GroupMember gm " +
            "   WHERE gm.group.groupId = :groupId AND gm.user.userId = :userId AND gm.exitedAt IS NULL" +
            ") AND gtc.isConnected = true")
    List<GroupTrackingCard> findConnectedCardsByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    List<GroupTrackingCard> findByMemberId(Long memberId);
}