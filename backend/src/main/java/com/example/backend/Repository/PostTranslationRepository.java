package com.example.backend.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.PostTranslation;
import com.example.backend.Utils.PostStatus;

public interface PostTranslationRepository extends JpaRepository<PostTranslation, Long> {
    
    @Query("SELECT pt FROM PostTranslation pt JOIN FETCH pt.post p " +
           "WHERE pt.slug = :slug AND pt.languageCode = :lang AND p.status = :status")
    Optional<PostTranslation> findBySlugAndLanguageCodeAndPostStatus(
            @Param("slug") String slug, 
            @Param("lang") String lang, 
            @Param("status") PostStatus status);

    boolean existsBySlug(String slug);
}