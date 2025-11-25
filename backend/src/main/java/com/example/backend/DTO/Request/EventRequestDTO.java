package com.example.backend.DTO.Request;

import java.time.LocalDateTime;

import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;

import lombok.Data;

@Data
public class EventRequestDTO {
    private String eventName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String bannerImageUrl;
    private EventStatus status;         
    private EventVisibility visibility; 
    private LocalDateTime registrationDeadline;
}