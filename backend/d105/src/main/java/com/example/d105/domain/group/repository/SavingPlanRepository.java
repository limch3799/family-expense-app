package com.example.d105.domain.group.repository;

import com.example.d105.domain.group.entity.SavingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SavingPlanRepository extends JpaRepository<SavingPlan, Long> {

    //특정 날짜와 상태로 저축 플랜 조회
    List<SavingPlan> findByTargetDateAndStatus(LocalDate targetDate, String status);

    List<SavingPlan> findByGroup_GroupId(Long groupId);

   List<SavingPlan> findByGroup_GroupIdAndStatus(Long groupId, String status);

}
