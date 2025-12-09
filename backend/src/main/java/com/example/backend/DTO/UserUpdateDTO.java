package com.example.backend.DTO;

import java.time.LocalDate;

import com.example.backend.Models.Gender;

import jakarta.validation.constraints.Past;
import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;
    private String address;
    private Gender gender;
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String avatarUrl;
}