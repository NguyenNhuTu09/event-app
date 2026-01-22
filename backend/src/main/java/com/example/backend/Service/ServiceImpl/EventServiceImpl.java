package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.ActivityEmailDTO;
import com.example.backend.DTO.Request.EventRegistrationRequestDTO;
import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventAttendeeDetailResponseDTO;
import com.example.backend.DTO.Response.EventAttendeeResponseDTO;
import com.example.backend.DTO.Response.EventResponseDTO;
import com.example.backend.DTO.Response.UserRegistrationHistoryDTO;
import com.example.backend.Exception.AccountLockedException;
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
import com.example.backend.Utils.EditRequestStatus;
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

        if (currentOrganizer.isLocked()) {
            throw new AccountLockedException("Tài khoản Organizer đang bị tạm khóa. Vui lòng gửi yêu cầu mở khóa để tiếp tục.");
        }

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
        event.setVisibility(requestDTO.getVisibility() != null ? requestDTO.getVisibility() : EventVisibility.PUBLIC);
        event.setCreatedAt(LocalDateTime.now());
        event.setStatus(EventStatus.DRAFT); 
        event.setEditLocked(false); 
        event.setEditRequestStatus(com.example.backend.Utils.EditRequestStatus.NONE);
        Event savedEvent = eventRepository.save(event);
        return convertToDTO(savedEvent);
    }


    @Override
    @Transactional
    public EventResponseDTO updateEvent(String slug, EventRequestDTO requestDTO) { 
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with slug: " + slug));

        Organizers currentOrganizer = getCurrentOrganizer();
        if (currentOrganizer.isLocked()) {
            throw new AccountLockedException("Tài khoản Organizer đang bị tạm khóa. Vui lòng gửi yêu cầu mở khóa để tiếp tục.");
        }
        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa sự kiện này.");
        }
        if (event.getStatus() == EventStatus.PUBLISHED && event.isEditLocked()) {
            throw new IllegalStateException("Sự kiện đã được công bố và đang bị khóa chỉnh sửa. Vui lòng gửi yêu cầu cấp quyền chỉnh sửa trước.");
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
        if (event.getStatus() == EventStatus.PUBLISHED) {
            event.setStatus(EventStatus.PENDING_APPROVAL);
        } 
        else if (requestDTO.getStatus() != null) {
            if (requestDTO.getStatus() == EventStatus.PUBLISHED) {
                event.setStatus(EventStatus.PENDING_APPROVAL);
            } else {
                event.setStatus(requestDTO.getStatus());
            }
        }        
        return convertToDTO(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with slug: " + slug));
        
        Organizers currentOrganizer = getCurrentOrganizer();
        if (currentOrganizer.isLocked()) {
            throw new AccountLockedException("Tài khoản Organizer đang bị tạm khóa. Vui lòng gửi yêu cầu mở khóa để tiếp tục.");
        }
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
        return eventRepository.findByStatusNot(EventStatus.DRAFT)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getMyEvents() {
        Organizers currentOrganizer = getCurrentOrganizer();
        return eventRepository.findByOrganizer_OrganizerId(currentOrganizer.getOrganizerId())
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<EventResponseDTO> getPublicEvents() {
        List<Event> events = eventRepository.findByStatusAndVisibility(EventStatus.PUBLISHED, EventVisibility.PUBLIC);
        LocalDateTime now = LocalDateTime.now();
        
        return events.stream()
                .filter(event -> event.getEndDate().isAfter(now)) // Lọc bỏ sự kiện đã qua
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
                .isFeatured(event.isFeatured())
                .isUpcoming(event.isUpcoming())
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
        event.setEditLocked(true); 
        event.setEditRequestStatus(EditRequestStatus.NONE); 
        event.setEditRequestReason(null);
        Event savedEvent = eventRepository.save(event);
        try {
            String organizerEmail = event.getOrganizer().getUser().getEmail();
            String organizerName = event.getOrganizer().getName();

            emailService.sendEventApprovedEmail(
                organizerEmail,
                organizerName,
                savedEvent.getEventName(),
                savedEvent.getStartDate(),
                savedEvent.getSlug()
            );
        } catch (Exception e) {
            System.err.println("Không thể gửi mail approve: " + e.getMessage());
        }
        return convertToDTO(savedEvent);
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
        Event savedEvent = eventRepository.save(event);

        try {
            String organizerEmail = event.getOrganizer().getUser().getEmail();
            String organizerName = event.getOrganizer().getName();
            
            String finalReason = (reason != null && !reason.trim().isEmpty()) 
                               ? reason 
                               : "Không đáp ứng tiêu chuẩn cộng đồng.";

            emailService.sendEventRejectedEmail(
                organizerEmail,
                organizerName,
                savedEvent.getEventName(),
                finalReason
            );
        } catch (Exception e) {
            System.err.println("Không thể gửi mail reject: " + e.getMessage());
        }

        event.setEditLocked(false);
        return convertToDTO(savedEvent);
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

        LocalDateTime now = LocalDateTime.now();

        
        if (now.isAfter(event.getEndDate())) {
            throw new IllegalArgumentException("Sự kiện đã kết thúc, không thể đăng ký.");
        }
        
        if (event.getRegistrationDeadline() != null && now.isAfter(event.getRegistrationDeadline())) {
            throw new IllegalArgumentException("Đã hết thời hạn đăng ký tham gia sự kiện này.");
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

    // @Override
    // @Transactional
    // public void approveRegistration(Long registrationId) {
    //     EventAttendees registration = eventAttendeesRepository.findById(registrationId)
    //             .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
                
    //     Organizers currentOrganizer = getCurrentOrganizer();

    //     if (currentOrganizer.isLocked()) {
    //         throw new AccountLockedException("Tài khoản Organizer đang bị tạm khóa. Vui lòng gửi yêu cầu mở khóa để tiếp tục.");
    //     }
        
    //     if (!registration.getEvent().getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
    //         throw new RuntimeException("Bạn không có quyền duyệt vé này.");
    //     }

    //     registration.setStatus(RegistrationStatus.APPROVED);
    //     eventAttendeesRepository.save(registration);

    //     List<ActivityAttendees> activities = activityAttendeesRepository.findByEventAttendee(registration);

    //     List<ActivityEmailDTO> activityEmailList = new ArrayList<>();
    //     if (activities != null) {
    //         activityEmailList = activities.stream()
    //                 .map(this::mapActivityToDTO) // Gọi hàm riêng
    //                 .collect(Collectors.toList());
    //     }
    //     emailService.sendRegistrationApprovedEmail(
    //         registration.getUser().getEmail(),
    //         registration.getUser().getUsername(),
    //         registration.getEvent().getEventName(),
    //         registration.getEvent().getStartDate(), 
    //         registration.getEvent().getEndDate(),
    //         registration.getEvent().getLocation(),
    //         registration.getTicketCode(),
    //         activityEmailList
    //     );
    // }

    @Override
    @Transactional
    public void approveRegistration(Long registrationId) {
        // 1. Lấy thông tin vé
        EventAttendees registration = eventAttendeesRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
                
        Organizers currentOrganizer = getCurrentOrganizer();

        if (currentOrganizer.isLocked()) {
            throw new AccountLockedException("Tài khoản Organizer đang bị tạm khóa.");
        }
        
        if (!registration.getEvent().getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền duyệt vé này.");
        }

        if (registration.getStatus() != RegistrationStatus.APPROVED) {
            registration.setStatus(RegistrationStatus.APPROVED);
            eventAttendeesRepository.save(registration);
        }

        List<ActivityAttendees> activities = activityAttendeesRepository.findByEventAttendee(registration);
        List<ActivityEmailDTO> activityEmailList = new ArrayList<>();

        if (activities != null) {
            for (ActivityAttendees actAttendee : activities) {
                if (actAttendee.getStatus() == RegistrationStatus.PENDING) {
                    actAttendee.setStatus(RegistrationStatus.APPROVED);
                    activityAttendeesRepository.save(actAttendee);
                }

                if (actAttendee.getStatus() == RegistrationStatus.APPROVED) {
                    activityEmailList.add(mapActivityToDTO(actAttendee));
                }
            }
        }

        try {
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
        } catch (Exception e) {
            System.err.println("Lỗi gửi email approve: " + e.getMessage());
        }
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

        if (currentOrganizer.isLocked()) {
            throw new AccountLockedException("Tài khoản Organizer đang bị tạm khóa. Vui lòng gửi yêu cầu mở khóa để tiếp tục.");
        }
        
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

    @Override
    public List<EventResponseDTO> getFeaturedEvents() {
        List<Event> events = eventRepository.findByIsFeaturedTrueAndStatusAndVisibility(
                EventStatus.PUBLISHED, EventVisibility.PUBLIC);
        return events.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<EventResponseDTO> updateFeaturedEvents(List<Long> eventIds) {
        if (eventIds.size() > 4) {
            throw new IllegalArgumentException("Chỉ được chọn tối đa 4 sự kiện nổi bật.");
        }

        List<Event> currentFeatured = eventRepository.findByIsFeaturedTrue();
        for (Event event : currentFeatured) {
            event.setFeatured(false);
        }
        eventRepository.saveAll(currentFeatured);

        List<Event> newFeatured = eventRepository.findAllById(eventIds);

        LocalDateTime now = LocalDateTime.now();
        
        if (newFeatured.size() != eventIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều ID sự kiện không tồn tại.");
        }

        for (Event event : newFeatured) {
            if (event.getStatus() != EventStatus.PUBLISHED) {
                throw new IllegalArgumentException("Sự kiện " + event.getEventName() + " chưa được công bố, không thể set nổi bật.");
            }
            if (event.getEndDate().isBefore(now)) {
                throw new IllegalArgumentException("Sự kiện '" + event.getEventName() + "' đã kết thúc, không thể chọn làm sự kiện Nổi bật.");
            }
            event.setFeatured(true);
        }
        
        List<Event> savedEvents = eventRepository.saveAll(newFeatured);
        return savedEvents.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // --- LOGIC SỰ KIỆN SẮP DIỄN RA (UPCOMING - MAX 8) ---

    @Override
    public List<EventResponseDTO> getUpcomingEvents() {
        List<Event> events = eventRepository.findByIsUpcomingTrueAndStatusAndVisibility(
                EventStatus.PUBLISHED, EventVisibility.PUBLIC);
        return events.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<EventResponseDTO> updateUpcomingEvents(List<Long> eventIds) {
        if (eventIds.size() > 8) {
            throw new IllegalArgumentException("Chỉ được chọn tối đa 8 sự kiện sắp diễn ra.");
        }

        List<Event> currentUpcoming = eventRepository.findByIsUpcomingTrue();
        for (Event event : currentUpcoming) {
            event.setUpcoming(false);
        }
        eventRepository.saveAll(currentUpcoming);

        List<Event> newUpcoming = eventRepository.findAllById(eventIds);
        
        if (newUpcoming.size() != eventIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều ID sự kiện không tồn tại.");
        }

        LocalDateTime now = LocalDateTime.now();

        for (Event event : newUpcoming) {
            if (event.getStatus() != EventStatus.PUBLISHED) {
                throw new IllegalArgumentException("Sự kiện " + event.getEventName() + " chưa được công bố.");
            }
            if (event.getEndDate().isBefore(now)) {
                throw new IllegalArgumentException("Sự kiện '" + event.getEventName() + "' đã kết thúc, không thể chọn làm sự kiện Sắp diễn ra.");
            }
            event.setUpcoming(true);
        }

        List<Event> savedEvents = eventRepository.saveAll(newUpcoming);
        return savedEvents.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public EventResponseDTO submitEventForApproval(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with slug: " + slug));

        Organizers currentOrganizer = getCurrentOrganizer();

        if (currentOrganizer.isLocked()) {
            throw new AccountLockedException("Tài khoản Organizer đang bị tạm khóa. Vui lòng gửi yêu cầu mở khóa để tiếp tục.");
        }
        
        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền gửi yêu cầu duyệt cho sự kiện này.");
        }

        if (event.getStatus() != EventStatus.DRAFT && event.getStatus() != EventStatus.REJECTED) {
            throw new IllegalArgumentException("Sự kiện này đang chờ duyệt hoặc đã được công bố, không thể gửi yêu cầu.");
        }

        event.setStatus(EventStatus.PENDING_APPROVAL);
        Event savedEvent = eventRepository.save(event);

        try {
            String organizerEmail = currentOrganizer.getUser().getEmail();
            String organizerName = currentOrganizer.getUser().getUsername();

            emailService.sendEventSubmissionPending(
                organizerEmail,
                organizerName,
                savedEvent.getEventName(),
                java.time.LocalDateTime.now()
            );
        } catch (Exception e) {
            System.err.println("Lỗi gửi email chờ duyệt: " + e.getMessage());
        }

        return convertToDTO(savedEvent);
    }

    @Override
    public List<UserRegistrationHistoryDTO> getMyRegistrationHistory() {
        User currentUser = getCurrentUser();

        List<EventAttendees> registrations = eventAttendeesRepository.findByUser_IdOrderByRegistrationDateDesc(currentUser.getId());

        return registrations.stream().map(reg -> {
            Event event = reg.getEvent();
            return UserRegistrationHistoryDTO.builder()
                    .eventId(event.getEventId())
                    .eventName(event.getEventName())
                    .slug(event.getSlug())
                    .bannerImageUrl(event.getBannerImageUrl())
                    .location(event.getLocation())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .organizerName(event.getOrganizer().getName())
                    
                    // Map thông tin đăng ký
                    .registrationId(reg.getId())
                    .status(reg.getStatus())
                    .ticketCode(reg.getStatus() == RegistrationStatus.APPROVED ? reg.getTicketCode() : null)
                    .registrationDate(reg.getRegistrationDate())
                    .eventCheckInStatus(reg.getEventCheckInStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleNewsletterSubscription(boolean subscribe) {
        User currentUser = getCurrentUser(); 
        currentUser.setSubscribedNews(subscribe);
        userRepository.save(currentUser);
    }

    @Override
    @Transactional
    public void addActivitiesToRegistration(Long eventId, List<Integer> activityIds) {
        User currentUser = getCurrentUser();
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện"));

        EventAttendees registration = eventAttendeesRepository.findByEventAndUser(event, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa đăng ký tham gia sự kiện này. Vui lòng đăng ký vé trước."));

        if (registration.getStatus() == RegistrationStatus.REJECTED) {
            throw new IllegalArgumentException("Vé của bạn đã bị từ chối, không thể đăng ký thêm hoạt động.");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (event.getEndDate().isBefore(now)) {
            throw new IllegalArgumentException("Sự kiện đã kết thúc.");
        }

        boolean hasNewPendingActivity = false;

        if (activityIds != null && !activityIds.isEmpty()) {
            for (Integer activityId : activityIds) {
                Activity activity = activityRepository.findById(activityId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hoạt động ID: " + activityId));

                if (!activity.getEvent().getEventId().equals(eventId)) {
                    throw new IllegalArgumentException("Hoạt động " + activity.getActivityName() + " không thuộc sự kiện này.");
                }

                boolean alreadyRegistered = activityAttendeesRepository.existsByEventAttendee_IdAndActivity_ActivityId(registration.getId(), activityId);
                if (alreadyRegistered) {
                    continue; 
                }

                if (activity.getMaxAttendees() != null) {
                    long currentCount = activityAttendeesRepository.countByActivity_ActivityId(activityId);
                    if (currentCount >= activity.getMaxAttendees()) {
                        throw new IllegalArgumentException("Hoạt động " + activity.getActivityName() + " đã hết chỗ.");
                    }
                }

                ActivityAttendees newActAttendee = new ActivityAttendees();
                newActAttendee.setEventAttendee(registration);
                newActAttendee.setActivity(activity);
                newActAttendee.setActCheckInStatus(CheckInStatus.NOT_CHECKED_IN);
                newActAttendee.setRegisteredAt(LocalDateTime.now()); // Set thời gian đăng ký

                if (registration.getEventCheckInStatus() == CheckInStatus.CHECKED_IN) {
                    newActAttendee.setStatus(RegistrationStatus.APPROVED);
                } else {
                    newActAttendee.setStatus(RegistrationStatus.PENDING);
                    hasNewPendingActivity = true;
                }
                
                activityAttendeesRepository.save(newActAttendee);
            }
        }
        if (hasNewPendingActivity 
            && registration.getStatus() == RegistrationStatus.APPROVED 
            && registration.getEventCheckInStatus() != CheckInStatus.CHECKED_IN) {
            
            registration.setStatus(RegistrationStatus.PENDING);
            eventAttendeesRepository.save(registration);
        }
    }


    @Override
    public EventAttendeeDetailResponseDTO getEventAttendeeDetail(Long registrationId) {
        EventAttendees registration = eventAttendeesRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin đăng ký (Vé) với ID: " + registrationId));

        Organizers currentOrganizer = getCurrentOrganizer();
        if (!registration.getEvent().getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền xem chi tiết vé của sự kiện này.");
        }

        List<ActivityAttendees> activityAttendeesList = activityAttendeesRepository.findByEventAttendee(registration);

        List<EventAttendeeDetailResponseDTO.RegisteredActivityDTO> activityDTOs = activityAttendeesList.stream()
                .map(aa -> EventAttendeeDetailResponseDTO.RegisteredActivityDTO.builder()
                        .activityId(aa.getActivity().getActivityId())
                        .activityName(aa.getActivity().getActivityName())
                        .startTime(aa.getActivity().getStartTime())
                        .endTime(aa.getActivity().getEndTime())
                        .roomOrVenue(aa.getActivity().getRoomOrVenue())
                        .activityStatus(aa.getStatus()) 
                        .activityCheckInStatus(aa.getActCheckInStatus())
                        .activityImageUrl(aa.getActivity().getActivityImageUrl())
                        .build())
                .collect(Collectors.toList());

        return EventAttendeeDetailResponseDTO.builder()
                .id(registration.getId())
                .ticketCode(registration.getTicketCode())
                .status(registration.getStatus())
                .registrationDate(registration.getRegistrationDate())
                .eventCheckInStatus(registration.getEventCheckInStatus())
                .userId(registration.getUser().getId())
                .username(registration.getUser().getUsername())
                .email(registration.getUser().getEmail())
                .phoneNumber(registration.getUser().getPhoneNumber())
                .avatarUrl(registration.getUser().getAvatarUrl())
                .activities(activityDTOs)
                .build();
    }

    @Override
    @Transactional
    public void requestEditPermission(Long eventId, String reason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        Organizers currentOrganizer = getCurrentOrganizer();

        if (!event.getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền gửi yêu cầu cho sự kiện này.");
        }

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new IllegalArgumentException("Chỉ có thể gửi yêu cầu chỉnh sửa cho sự kiện ĐÃ CÔNG BỐ.");
        }

        if (LocalDateTime.now().isAfter(event.getStartDate())) {
            throw new IllegalArgumentException("Sự kiện đã hoặc đang diễn ra, không thể yêu cầu chỉnh sửa.");
        }

        if (event.getEditRequestStatus() == EditRequestStatus.PENDING) {
            throw new IllegalArgumentException("Đang có một yêu cầu chờ duyệt, vui lòng đợi.");
        }

        event.setEditRequestStatus(EditRequestStatus.PENDING);
        event.setEditRequestReason(reason);
        eventRepository.save(event);
        try {
            emailService.sendEditRequestPendingEmail(
                currentOrganizer.getUser().getEmail(),
                currentOrganizer.getUser().getUsername(),
                event.getEventName(),
                reason
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void approveEditPermission(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getEditRequestStatus() != EditRequestStatus.PENDING) {
            throw new IllegalArgumentException("Không có yêu cầu chỉnh sửa nào đang chờ duyệt.");
        }

        event.setEditLocked(false);
        event.setEditRequestStatus(EditRequestStatus.APPROVED);
        
        eventRepository.save(event);

        try {
            emailService.sendEditRequestApprovedEmail(
                event.getOrganizer().getUser().getEmail(),
                event.getOrganizer().getUser().getUsername(),
                event.getEventName(),
                event.getSlug()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void rejectEditPermission(Long eventId, String rejectReason) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getEditRequestStatus() != EditRequestStatus.PENDING) {
            throw new IllegalArgumentException("Không có yêu cầu chỉnh sửa nào đang chờ duyệt.");
        }

        event.setEditRequestStatus(EditRequestStatus.REJECTED);
        
        eventRepository.save(event);

        try {
            emailService.sendEditRequestRejectedEmail(
                event.getOrganizer().getUser().getEmail(),
                event.getOrganizer().getUser().getUsername(),
                event.getEventName(),
                rejectReason
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}