package com.example.d105.read.repository;

import com.example.d105.read.entity.GroupMonthlyTotals;
import com.example.d105.read.entity.GroupMonthlyTotalsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMonthlyTotalsRepository extends JpaRepository<GroupMonthlyTotals, GroupMonthlyTotalsId> {

    Optional<GroupMonthlyTotals> findByGroupIdAndYearMonth(Long groupId, String yearMonth);

    @Modifying
    @Query(value = """
        INSERT INTO d105_read.group_monthly_totals 
        (group_id, year_month, total_amount, active_member_count, avg_amount_per_member, last_updated) 
        VALUES (:groupId, :yearMonth, :totalAmount, :activeMemberCount, :avgAmountPerMember, now())
        ON CONFLICT (group_id, year_month) 
        DO UPDATE SET 
            total_amount = EXCLUDED.total_amount,
            active_member_count = EXCLUDED.active_member_count,
            avg_amount_per_member = EXCLUDED.avg_amount_per_member,
            last_updated = now()
        """, nativeQuery = true)
    void upsertGroupMonthlyTotals(
            @Param("groupId") Long groupId,
            @Param("yearMonth") String yearMonth,
            @Param("totalAmount") Long totalAmount,
            @Param("activeMemberCount") Integer activeMemberCount,
            @Param("avgAmountPerMember") Long avgAmountPerMember
    );
}