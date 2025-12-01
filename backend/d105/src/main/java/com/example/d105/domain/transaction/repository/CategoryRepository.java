package com.example.d105.domain.transaction.repository;

import com.example.d105.domain.transaction.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Short> {
    // 기본 CRUD 메서드만 사용
    /**
     * 카테고리 존재 여부 확인
     */
    boolean existsById(Short categoryId);
}