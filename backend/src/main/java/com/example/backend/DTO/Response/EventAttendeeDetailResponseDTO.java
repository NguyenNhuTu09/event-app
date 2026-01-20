package com.example.backend.DTO.Response;

import java.time.LocalDateTime;
import java.util.List;

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
public class EventAttendeeDetailResponseDTO {
    private Long id; 
    private String ticketCode;
    private RegistrationStatus status;
    private LocalDateTime registrationDate;
    private CheckInStatus eventCheckInStatus;

    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;

    private List<RegisteredActivityDTO> activities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisteredActivityDTO {
        private Integer activityId;
        private String activityName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String roomOrVenue;
        private RegistrationStatus activityStatus; 
        private CheckInStatus activityCheckInStatus; 
        private String activityImageUrl;
    }
}