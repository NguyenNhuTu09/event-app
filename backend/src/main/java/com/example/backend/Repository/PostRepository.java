package com.example.backend.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Models.Entity.Post;
import com.example.backend.Utils.PostStatus;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByStatusOrderByCreatedAtDesc(PostStatus status, Pageable pageable);
    Optional<Post> findBySlugAndStatus(String slug, PostStatus status);
    boolean existsBySlug(String slug);
}
