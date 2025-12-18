package com.example.backend.DTO.Response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventCheckInResultDTO {
    private EventAttendeeResponseDTO attendee;
    
    private EventResponseDTO event;

    private List<ActivityResponseDTO> agenda;
}