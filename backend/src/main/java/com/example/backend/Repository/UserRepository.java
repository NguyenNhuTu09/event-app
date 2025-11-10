package com.example.backend.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.DTO.UserResponseDTO;
import com.example.backend.Models.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    UserResponseDTO findUserByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}