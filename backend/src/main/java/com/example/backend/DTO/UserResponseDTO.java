package com.example.backend.DTO;

import java.time.LocalDate;

import com.example.backend.Models.Gender;
import com.example.backend.Utils.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String address;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String avatarUrl;
    private Role role;
}