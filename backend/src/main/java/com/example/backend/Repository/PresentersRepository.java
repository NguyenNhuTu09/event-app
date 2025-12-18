package com.example.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.Presenters;

public interface PresentersRepository extends JpaRepository<Presenters, Integer> {
    Presenters findByFullName(String fullName);
    // Presenters findByEmail(String email);
    List<Presenters> findByFullNameContainingIgnoreCase(String name);
    List<Presenters> findByCompany(String company);
    boolean existsByFullNameAndCompany(String fullName, String company);

    @Query("SELECT p FROM Presenters p WHERE " +
           "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.company) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Presenters> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT a.presenter FROM Activity a " +
           "WHERE a.event.organizer.slug = :slug " +
           "AND a.presenter IS NOT NULL")
    List<Presenters> findByOrganizerSlug(@Param("slug") String slug);
}
