package com.example.backend.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.backend.DTO.UserResponseDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String accessToken;
    private String refreshToken;

    private String tokenType = "Bearer";
    private UserResponseDTO user; 
}
