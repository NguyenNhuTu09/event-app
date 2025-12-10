package com.example.backend.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.DTO.UserResponseDTO;
import com.example.backend.Models.Entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    UserResponseDTO findUserByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
    @Query("SELECT u FROM User u WHERE u.username = :keyword OR u.email = :keyword")
    Optional<User> findByUsernameOrEmail(@Param("keyword") String keyword);
    Optional<User> findByUid(String uid);
    boolean existsByUid(String uid);
}