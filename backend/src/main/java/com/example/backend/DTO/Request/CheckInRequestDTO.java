package com.example.backend.DTO.Request;

import lombok.Data;

@Data
public class CheckInRequestDTO {
    private String ticketCode;      // Dành cho Organizer check-in user
    private String activityQrCode;  // Dành cho User check-in activity
    
    private Double latitude;
    private Double longitude;
}