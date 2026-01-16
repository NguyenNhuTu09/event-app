package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.EventRegistrationRequestDTO;
import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventAttendeeDetailResponseDTO;
import com.example.backend.DTO.Response.EventAttendeeResponseDTO;
import com.example.backend.DTO.Response.EventResponseDTO;
import com.example.backend.DTO.Response.UserRegistrationHistoryDTO;

public interface EventService {
    EventResponseDTO createEvent(EventRequestDTO requestDTO);
    EventResponseDTO updateEvent(String slug, EventRequestDTO requestDTO);
    void deleteEvent(String slug);
    EventResponseDTO getEventBySlug(String slug);
    List<EventResponseDTO> getAllEvents(); 
    List<EventResponseDTO> getMyEvents();  
    List<EventResponseDTO> getPublicEvents(); 
    EventResponseDTO approveEvent(Long eventId);
    EventResponseDTO rejectEvent(Long eventId, String reason);
    EventResponseDTO submitEventForApproval(String slug); 

    List<UserRegistrationHistoryDTO> getMyRegistrationHistory();
    
    void registerForEvent(EventRegistrationRequestDTO requestDTO);
    List<EventAttendeeResponseDTO> getEventRegistrations(Long eventId);
    void approveRegistration(Long registrationId);
    void rejectRegistration(Long registrationId, String reason);

    List<EventResponseDTO> getFeaturedEvents();
    List<EventResponseDTO> updateFeaturedEvents(List<Long> eventIds);

    List<EventResponseDTO> getUpcomingEvents();
    List<EventResponseDTO> updateUpcomingEvents(List<Long> eventIds);

    void toggleNewsletterSubscription(boolean subscribe);

    void addActivitiesToRegistration(Long eventId, List<Integer> activityIds);

    EventAttendeeDetailResponseDTO getEventAttendeeDetail(Long registrationId);
}