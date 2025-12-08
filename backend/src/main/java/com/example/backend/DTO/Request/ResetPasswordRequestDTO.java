package com.example.backend.DTO.Request;

import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}
