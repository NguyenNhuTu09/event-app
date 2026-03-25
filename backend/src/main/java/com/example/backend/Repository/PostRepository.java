package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.Post;
import com.example.backend.Utils.PostStatus;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByStatusOrderByCreatedAtDesc(PostStatus status, Pageable pageable);
    // Optional<Post> findBySlugAndStatus(String slug, PostStatus status);
    // boolean existsBySlug(String slug);

    // Query 1: Load post + post.translations + author
    @Query("SELECT DISTINCT p FROM Post p " +
        "LEFT JOIN FETCH p.translations " +
        "LEFT JOIN FETCH p.author " +
        "WHERE p.status = :status " +
        "ORDER BY p.createdAt DESC")
    List<Post> findPublishedWithTranslations(@Param("status") PostStatus status);

    // Query 2: Load post + category + category.translations (riêng biệt)
    @Query("SELECT DISTINCT p FROM Post p " +
        "LEFT JOIN FETCH p.category c " +
        "LEFT JOIN FETCH c.translations " +
        "WHERE p.status = :status")
    List<Post> findPublishedWithCategory(@Param("status") PostStatus status);

    // Lấy post theo slug có JOIN FETCH category
    @Query("SELECT p FROM Post p " +
        "LEFT JOIN FETCH p.category c " +
        "LEFT JOIN FETCH c.translations " +
        "WHERE p.id = :postId")
    Optional<Post> findByIdWithCategory(@Param("postId") Long postId);
}
