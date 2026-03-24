package com.example.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Models.Entity.CategoryTranslation;

public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, Long> {
    // boolean existsBySlug(String slug); // nếu sau này cần
}