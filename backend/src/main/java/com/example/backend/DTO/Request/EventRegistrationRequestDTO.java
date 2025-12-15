package com.example.backend.DTO.Request;

import java.util.List;

import lombok.Data;

@Data
public class EventRegistrationRequestDTO {
    private Long eventId;
    private List<Integer> activityIds; 
}