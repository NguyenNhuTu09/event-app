package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;

public interface OrganizersRepository extends JpaRepository<Organizers, Integer> {
    Optional<Organizers> findByName(String name);
    Optional<Organizers> findByContactEmail(String email);

    List<Organizers> findByUser_Id(Long userId);
    Optional<Organizers> findByUser_Username(String username);
    Optional<Organizers> findByUser_Email(String email);

    boolean existsByName(String name);
    boolean existsByContactEmail(String email);
    boolean existsByUser_Username(String username);

    boolean existsByUser(User user);
    boolean existsByUser_Id(Long userId);

    Optional<Organizers> findBySlug(String slug);
    boolean existsBySlug(String slug);
    void deleteBySlug(String slug);
}
