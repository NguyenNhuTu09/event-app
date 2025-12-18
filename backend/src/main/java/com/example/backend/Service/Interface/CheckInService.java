package com.example.backend.Service.Interface;

import com.example.backend.DTO.Response.EventCheckInResultDTO;

public interface CheckInService {
    EventCheckInResultDTO organizerCheckInUser(String ticketCode);
    String userCheckInActivity(String activityQrCode);
}