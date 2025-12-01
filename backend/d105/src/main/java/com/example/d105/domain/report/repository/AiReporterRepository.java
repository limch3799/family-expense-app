package com.example.d105.domain.report.repository;

import com.example.d105.domain.report.entity.AiReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiReporterRepository extends JpaRepository<AiReport, Long> {

    List<AiReport> findByGroup_GroupId(Long groupId);

    boolean existsByGroup_GroupIdAndYearMonth(Long groupId, String yearMonth);
}
