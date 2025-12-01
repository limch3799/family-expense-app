package com.example.d105.read.repository;

import com.example.d105.read.entity.GroupMonthlyExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMonthlyExpenseStatusRepository extends JpaRepository<GroupMonthlyExpenseStatus, Long> {

    //특정 그룹의 데이터 (지출 금액순 정렬)
    List<GroupMonthlyExpenseStatus> findByGroupIdAndYearMonthOrderByTotalExpenseDesc(Long groupId, String yearMonth);
}
