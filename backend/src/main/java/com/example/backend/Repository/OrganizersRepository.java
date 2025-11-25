package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Models.Entity.Organizers;

public interface OrganizersRepository extends JpaRepository<Organizers, Integer> {
    Optional<Organizers> findByName(String name);
    Optional<Organizers> findByContactEmail(String email);

    List<Organizers> findByUser_Id(Long userId);
    Optional<Organizers> findByUser_Username(String username);
    boolean existsByName(String name);
    boolean existsByContactEmail(String email);
    boolean existsByUser_Username(String username);

}
