package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventResponseDTO;

public interface EventService {
    EventResponseDTO createEvent(EventRequestDTO requestDTO);
    EventResponseDTO updateEvent(Long eventId, EventRequestDTO requestDTO);
    void deleteEvent(Long eventId);
    EventResponseDTO getEventById(Long eventId);
    List<EventResponseDTO> getAllEvents(); 
    List<EventResponseDTO> getMyEvents();  
    List<EventResponseDTO> getPublicEvents(); 

    EventResponseDTO approveEvent(Long eventId);
    EventResponseDTO rejectEvent(Long eventId, String reason);
}