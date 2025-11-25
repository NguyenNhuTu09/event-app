package com.example.backend.Service.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Service.Interface.EventService;
import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final OrganizersRepository organizersRepository;

    private Organizers getCurrentOrganizer() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Organizers organizer = organizersRepository.findByUser_Username(username)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký làm Organizer hoặc tài khoản không tồn tại."));
        
        if (!organizer.isApproved()) {
            throw new IllegalArgumentException("Tài khoản Organizer của bạn chưa được phê duyệt.");
        }
        return organizer;
    }

    @Override
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO requestDTO) {
        Organizers currentOrganizer = getCurrentOrganizer();

        Event event = new Event();
        event.setOrganizer(currentOrganizer); 
        event.setEventName(requestDTO.getEventName());
        event.setDescription(requestDTO.getDescription());
        event.setStartDate(requestDTO.getStartDate());
        event.setEndDate(requestDTO.getEndDate());
        event.setLocation(requestDTO.getLocation());
        event.setBannerImageUrl(requestDTO.getBannerImageUrl());
        event.setRegistrationDeadline(requestDTO.getRegistrationDeadline());
        
        event.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : EventStatus.DRAFT);
        event.setVisibility(requestDTO.getVisibility() != null ? requestDTO.getVisibility() : EventVisibility.PUBLIC);

        if (requestDTO.getStatus() == EventStatus.PUBLISHED) {
        event.setStatus(EventStatus.PENDING_APPROVAL);
        } else {
            event.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : EventStatus.DRAFT);
        }

        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }

    @Override
    @Transactional
    public EventResponseDTO updateEvent(Long eventId, EventRequestDTO requestDTO) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Organizers currentOrganizer = getCurrentOrganizer();
        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa sự kiện này.");
        }

        event.setEventName(requestDTO.getEventName());
        event.setDescription(requestDTO.getDescription());
        event.setStartDate(requestDTO.getStartDate());
        event.setEndDate(requestDTO.getEndDate());
        event.setLocation(requestDTO.getLocation());
        event.setBannerImageUrl(requestDTO.getBannerImageUrl());
        event.setRegistrationDeadline(requestDTO.getRegistrationDeadline());
        if(requestDTO.getStatus() != null) event.setStatus(requestDTO.getStatus());
        if(requestDTO.getVisibility() != null) event.setVisibility(requestDTO.getVisibility());

        if (requestDTO.getStatus() == EventStatus.PUBLISHED) {
            event.setStatus(EventStatus.PENDING_APPROVAL);
        } else if (requestDTO.getStatus() != null) {
            event.setStatus(requestDTO.getStatus());
        }
        return convertToDTO(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        Organizers currentOrganizer = getCurrentOrganizer();
        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
             throw new RuntimeException("Bạn không có quyền xóa sự kiện này.");
        }
        eventRepository.delete(event);
    }

    @Override
    public EventResponseDTO getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return convertToDTO(event);
    }

    @Override
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getMyEvents() {
        Organizers currentOrganizer = getCurrentOrganizer();
        return eventRepository.findByOrganizer_OrganizerId(currentOrganizer.getOrganizerId())
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getPublicEvents() {
        return eventRepository.findByStatusAndVisibility(EventStatus.PUBLISHED, EventVisibility.PUBLIC)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private EventResponseDTO convertToDTO(Event event) {
        return EventResponseDTO.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .bannerImageUrl(event.getBannerImageUrl())
                .status(event.getStatus())
                .visibility(event.getVisibility())
                .registrationDeadline(event.getRegistrationDeadline())
                .organizerId(event.getOrganizer().getOrganizerId())
                .organizerName(event.getOrganizer().getName())
                .build();
    }

    @Override
    @Transactional
    public EventResponseDTO approveEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Sự kiện này đã được công bố rồi.");
        }

        event.setStatus(EventStatus.PUBLISHED); 
        return convertToDTO(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponseDTO rejectEvent(Long eventId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Không thể từ chối sự kiện đã công bố.");
        }
        event.setStatus(EventStatus.REJECTED);
        return convertToDTO(eventRepository.save(event));
    }
}