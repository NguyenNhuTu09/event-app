package com.example.backend.Models.Entity;

import java.time.LocalDate;

import com.example.backend.Models.Gender;
import com.example.backend.Utils.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password; 
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    private String address;
    
    @Enumerated(EnumType.STRING)
    private Gender gender; 
    
    private LocalDate dateOfBirth;
    
    private String phoneNumber;

    private String avatarUrl;

    private String avatarS3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}