package com.example.backend.DTO.Response;

import java.time.LocalDateTime;

import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.RegistrationStatus;

import lombok.Builder;

import lombok.Data;

@Data
@Builder
public class UserRegistrationHistoryDTO {
    private Long eventId;
    private String eventName;
    private String slug;
    private String bannerImageUrl;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String organizerName;

    private Long registrationId;
    private RegistrationStatus status;        
    private String ticketCode;                
    private LocalDateTime registrationDate;   
    private CheckInStatus eventCheckInStatus; 
}
