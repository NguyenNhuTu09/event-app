package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    List<Category> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);
    

    // THÊM: tìm category theo slug của translation
    @Query("SELECT c FROM Category c JOIN c.translations t WHERE t.slug = :slug")
    Optional<Category> findByTranslationSlug(@Param("slug") String slug);

    // THÊM: kiểm tra slug translation đã tồn tại chưa
    @Query("SELECT COUNT(t) > 0 FROM CategoryTranslation t WHERE t.slug = :slug")
    boolean existsByTranslationSlug(@Param("slug") String slug);
}