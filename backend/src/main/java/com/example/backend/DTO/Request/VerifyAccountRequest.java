package com.example.backend.DTO.Request;

import lombok.Data;

@Data
public class VerifyAccountRequest {
    private String email;
    private String verificationCode;
}
