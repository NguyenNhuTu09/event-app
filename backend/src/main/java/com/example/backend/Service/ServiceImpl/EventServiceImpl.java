package com.example.backend.Service.ServiceImpl;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.ActivityEmailDTO;
import com.example.backend.DTO.Request.EventRegistrationRequestDTO;
import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventAttendeeResponseDTO;
import com.example.backend.DTO.Response.EventResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Activity;
import com.example.backend.Models.Entity.ActivityAttendees;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.ActivityAttendeesRepository;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.EmailService;
import com.example.backend.Service.Interface.EventService;
import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;
import com.example.backend.Utils.RegistrationStatus;
import com.github.slugify.Slugify;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final OrganizersRepository organizersRepository;
    private final Slugify slugify = Slugify.builder().build();
    private final EventAttendeesRepository eventAttendeesRepository;
    private final UserRepository userRepository; 
    private final ActivityRepository activityRepository;
    private final ActivityAttendeesRepository activityAttendeesRepository;
    private final EmailService emailService;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String generateUniqueSlug(String eventName) {
        String baseSlug = slugify.slugify(eventName);
        String finalSlug = baseSlug;
        int count = 1;
        while (eventRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + count;
            count++;
        }
        return finalSlug;
    }

    private Organizers getCurrentOrganizer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentIdentity;

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            currentIdentity = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            currentIdentity = principal.toString();
        }

        final String emailToSearch = currentIdentity;

        Organizers organizer = organizersRepository.findByUser_Email(emailToSearch)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký làm Organizer hoặc tài khoản không tồn tại (Email: " + emailToSearch + ")."));
        
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
        event.setSlug(generateUniqueSlug(requestDTO.getEventName()));
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
    public EventResponseDTO updateEvent(String slug, EventRequestDTO requestDTO) { 
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with slug: " + slug));

        Organizers currentOrganizer = getCurrentOrganizer();
        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa sự kiện này.");
        }

        if (!event.getEventName().equals(requestDTO.getEventName())) {
            event.setEventName(requestDTO.getEventName());
            event.setSlug(generateUniqueSlug(requestDTO.getEventName()));
        }

        event.setDescription(requestDTO.getDescription());
        event.setStartDate(requestDTO.getStartDate());
        event.setEndDate(requestDTO.getEndDate());
        event.setLocation(requestDTO.getLocation());
        event.setBannerImageUrl(requestDTO.getBannerImageUrl());
        event.setRegistrationDeadline(requestDTO.getRegistrationDeadline());
        
        if(requestDTO.getVisibility() != null) event.setVisibility(requestDTO.getVisibility());

        if (requestDTO.getStatus() == EventStatus.PUBLISHED) {
            event.setStatus(EventStatus.PENDING_APPROVAL);
        } else if (requestDTO.getStatus() != null) {
            event.setStatus(requestDTO.getStatus());
        }
        return convertToDTO(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with slug: " + slug));
        
        Organizers currentOrganizer = getCurrentOrganizer();
        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
             throw new RuntimeException("Bạn không có quyền xóa sự kiện này.");
        }
        eventRepository.delete(event);
    }

    @Override
    public EventResponseDTO getEventBySlug(String slug) { 
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with slug: " + slug));
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
                .slug(event.getSlug())
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

    private EventAttendeeResponseDTO convertToAttendeeDTO(EventAttendees entity) {
        return EventAttendeeResponseDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .username(entity.getUser().getUsername())
                .email(entity.getUser().getEmail())
                .phoneNumber(entity.getUser().getPhoneNumber())
                .avatarUrl(entity.getUser().getAvatarUrl()) 
                .registrationDate(entity.getRegistrationDate())
                .status(entity.getStatus())
                .ticketCode(entity.getStatus() == RegistrationStatus.APPROVED ? entity.getTicketCode() : null)
                .eventCheckInStatus(entity.getEventCheckInStatus())
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


    @Override
    @Transactional
    public void registerForEvent(EventRegistrationRequestDTO requestDTO) {
        User currentUser = getCurrentUser();
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện"));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Sự kiện chưa được công bố.");
        }
        if (eventAttendeesRepository.existsByEventAndUser(event, currentUser)) {
            throw new IllegalArgumentException("Bạn đã đăng ký sự kiện này rồi.");
        }

        EventAttendees registration = new EventAttendees();
        registration.setUser(currentUser);
        registration.setEvent(event);
        registration.setStatus(RegistrationStatus.PENDING); 
        EventAttendees savedRegistration = eventAttendeesRepository.save(registration);

        if (requestDTO.getActivityIds() != null && !requestDTO.getActivityIds().isEmpty()) {
            for (Integer activityId : requestDTO.getActivityIds()) {
                Activity activity = activityRepository.findById(activityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hoạt động ID: " + activityId));

                if (!activity.getEvent().getEventId().equals(event.getEventId())) {
                    throw new IllegalArgumentException("Hoạt động " + activity.getActivityName() + " không thuộc sự kiện này.");
                }

                if (activity.getMaxAttendees() != null) {
                    long currentCount = activityAttendeesRepository.countByActivity_ActivityId(activityId);
                    if (currentCount >= activity.getMaxAttendees()) {
                        throw new IllegalArgumentException("Hoạt động " + activity.getActivityName() + " đã hết chỗ.");
                    }
                }

                ActivityAttendees actAttendee = new ActivityAttendees();
                actAttendee.setEventAttendee(savedRegistration); 
                actAttendee.setActivity(activity);
                actAttendee.setActCheckInStatus(CheckInStatus.NOT_CHECKED_IN);
                
                activityAttendeesRepository.save(actAttendee);

                emailService.sendRegistrationPendingEmail(
                    currentUser.getEmail(),
                    currentUser.getUsername(),
                    event.getEventName(),
                    event.getStartDate(), 
                    event.getEndDate(),   
                    event.getLocation()
                );
            }
        }
    }

    @Override
    public List<EventAttendeeResponseDTO> getEventRegistrations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
                
        Organizers currentOrganizer = getCurrentOrganizer();
        
        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền xem danh sách đăng ký của sự kiện này.");
        }
        
        List<EventAttendees> attendeesList = eventAttendeesRepository.findByEvent_EventId(eventId);
        
        return attendeesList.stream()
                .map(this::convertToAttendeeDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveRegistration(Long registrationId) {
        EventAttendees registration = eventAttendeesRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
                
        Organizers currentOrganizer = getCurrentOrganizer();
        
        if (!registration.getEvent().getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền duyệt vé này.");
        }

        registration.setStatus(RegistrationStatus.APPROVED);
        eventAttendeesRepository.save(registration);

        List<ActivityAttendees> activities = activityAttendeesRepository.findByEventAttendee(registration);

        List<ActivityEmailDTO> activityEmailList = new ArrayList<>();
        if (activities != null) {
            activityEmailList = activities.stream()
                    .map(this::mapActivityToDTO) // Gọi hàm riêng
                    .collect(Collectors.toList());
        }
        emailService.sendRegistrationApprovedEmail(
            registration.getUser().getEmail(),
            registration.getUser().getUsername(),
            registration.getEvent().getEventName(),
            registration.getEvent().getStartDate(), 
            registration.getEvent().getEndDate(),
            registration.getEvent().getLocation(),
            registration.getTicketCode(),
            activityEmailList
        );
    }

    private ActivityEmailDTO mapActivityToDTO(ActivityAttendees actAttendee) {
        Activity act = actAttendee.getActivity();
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        String timeDisplayStr;
        
        if (act.getStartTime() != null && act.getEndTime() != null) {
            boolean isSameDay = act.getStartTime().toLocalDate().isEqual(act.getEndTime().toLocalDate());
            if (isSameDay) {
                timeDisplayStr = act.getStartTime().format(timeFormatter) + " - " + 
                                 act.getEndTime().format(timeFormatter) + 
                                 " (" + act.getStartTime().format(dateFormatter) + ")";
            } else {
                timeDisplayStr = act.getStartTime().format(fullFormatter) + "  đến  " + 
                                 act.getEndTime().format(fullFormatter);
            }
        } else {
            timeDisplayStr = "Chưa cập nhật";
        }

        return ActivityEmailDTO.builder()
                .name(act.getActivityName())
                .timeRange(timeDisplayStr) 
                .location(act.getRoomOrVenue() != null ? act.getRoomOrVenue() : "Chưa cập nhật")
                .description(act.getDescription())
                .build();
    }

    @Override
    @Transactional
    public void rejectRegistration(Long registrationId, String reason) {
        EventAttendees registration = eventAttendeesRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
                
        Organizers currentOrganizer = getCurrentOrganizer();
        
        if (!registration.getEvent().getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền từ chối vé này.");
        }

        registration.setStatus(RegistrationStatus.REJECTED);
            eventAttendeesRepository.save(registration);
            emailService.sendRegistrationRejectedEmail(
            registration.getUser().getEmail(),
            registration.getUser().getUsername(),
            registration.getEvent().getEventName(),
            registration.getEvent().getStartDate(),
            registration.getEvent().getLocation(),
            reason
        );
    }
}