package com.example.backend.DTO.Response;

import java.time.LocalDateTime;

import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventResponseDTO {
    private Long eventId;
    private String eventName;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String bannerImageUrl;
    private EventStatus status;
    private EventVisibility visibility;
    private LocalDateTime registrationDeadline;
    
    private Integer organizerId;
    private String organizerName;
}