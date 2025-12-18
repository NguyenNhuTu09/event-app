package com.example.backend.Service.Interface;

import com.example.backend.DTO.Response.EventAttendeeResponseDTO;

public interface CheckInService {
    EventAttendeeResponseDTO organizerCheckInUser(String ticketCode);
    String userCheckInActivity(String activityQrCode);
}