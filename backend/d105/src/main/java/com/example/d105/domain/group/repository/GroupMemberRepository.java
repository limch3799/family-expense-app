package com.example.d105.domain.group.repository;

import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.userId = :userId AND gm.exitedAt IS NULL")
    Optional<GroupMember> findActiveGroupMemberByUserId(@Param("userId") Long userId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.groupId = :groupId AND gm.user.userId = :userId AND gm.exitedAt IS NULL")
    Optional<GroupMember> findByGroupIdAndUserIdAndActive(@Param("groupId") Long groupId, @Param("userId") Long userId);

    long countByGroup_GroupId(Long groupId);

    //exited가 null이 아닌 (그룹이 존재하는) groupMember가 존재하는지
    //해당 사용자에 가입된 그룹이 있는지
    boolean existsByUser_UserIdAndExitedAtIsNotNull(Long userId);


    //해당 user의 해당 그룹 맴버의 컬럼을 찾습니다
    List<GroupMember> findByGroup_GroupIdAndUser_UserId(Long groupId, Long userId);

    // 1. 특정 그룹에서 allowedAt과 rejectedAt이 모두 null인 멤버들 (대기 중인 멤버들)
    List<GroupMember> findByGroup_GroupIdAndAllowedAtIsNullAndRejectedAtIsNull(Long groupId);

    // 2. 특정 그룹에서 allowedAt이 null이 아닌 멤버들 (승인된 멤버들)
    List<GroupMember> findByGroup_GroupIdAndAllowedAtIsNotNull(Long groupId);

    List<GroupMember> findByUser(User user);

    boolean existsByUser_UserIdAndAllowedAtIsNotNullAndExitedAtIsNull(Long userId);

    boolean existsByGroup_GroupIdAndUser_UserIdAndAllowedAtIsNotNullAndExitedAtIsNull(Long groupId, Long userId);

    //그룹 id로 그룹맴버들 조회
    List<GroupMember> findByGroup(Group group);

    List<GroupMember> findByGroup_GroupId(Long groupId);
    //특정 그룹의 특정 맴버 ( 나가지 않은)
//    Optional<GroupMember> findByUser_UserIdAndGroup_GroupIdAndExitedAtIsNotNull(Long userId, Long groupId);

    /**
     * 특정 그룹의 활성 멤버 목록 조회 (그룹 분석용)
     */
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.groupId = :groupId AND gm.exitedAt IS NULL")
    List<GroupMember> findByGroupIdAndMemberStatus(@Param("groupId") Long groupId);

    /**
     * 사용자의 모든 활성 그룹 멤버십 조회 (그룹 집계용)
     */
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.userId = :userId AND gm.exitedAt IS NULL")
    List<GroupMember> findByUserIdAndMemberStatus(@Param("userId") Long userId);

    /**
     * userId로 해당 유저가 현재 가입된 활성 그룹의 groupId 조회
     * 사용자는 하나의 그룹에만 가입 가능
     * @param userId 사용자 ID
     * @return 활성 그룹 ID (없으면 Optional.empty())
     */
    @Query("SELECT gm.group.groupId FROM GroupMember gm " +
            "WHERE gm.user.userId = :userId " +
            "AND gm.exitedAt IS NULL " +
            "AND gm.allowedAt IS NOT NULL")
    Long findActiveGroupIdByUserId(@Param("userId") Long userId);
}
