package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Models.Entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
}