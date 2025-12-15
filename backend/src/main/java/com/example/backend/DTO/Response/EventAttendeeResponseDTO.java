package com.example.backend.DTO.Response;

import java.time.LocalDateTime;

import com.example.backend.Utils.RegistrationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendeeResponseDTO {
    
    private Long id;
    
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;

    private LocalDateTime registrationDate;
    private RegistrationStatus status; 
    private String ticketCode;         
    private boolean checkInStatus;    
}