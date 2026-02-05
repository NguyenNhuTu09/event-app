package com.example.backend.DTO.Response;

import java.time.LocalDateTime;

import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.RegistrationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityParticipantResponseDTO {
    private Long activityAttendeeId; 
    
    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;

    private RegistrationStatus registrationStatus; // PENDING, APPROVED, REJECTED
    private CheckInStatus checkInStatus;           // NOT_CHECKED_IN, CHECKED_IN
    private LocalDateTime checkInTime;             // Thời gian check-in
    private LocalDateTime registeredAt;            // Thời gian đăng ký
}