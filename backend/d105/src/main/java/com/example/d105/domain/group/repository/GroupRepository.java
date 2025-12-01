package com.example.d105.domain.group.repository;

import com.example.d105.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByInvitationCode(String invitationCode);

    Group findByInvitationCode(String invitationCode);

    @Query("SELECT g.groupId FROM Group g WHERE g.deletedAt IS NULL")
    List<Long> findGroupIdsByDeletedAtIsNull();
}
