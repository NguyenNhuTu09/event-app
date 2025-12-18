package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Response.EventAttendeeResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Activity;
import com.example.backend.Models.Entity.ActivityAttendees;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.ActivityAttendeesRepository;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.Interface.CheckInService;
import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.RegistrationStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService {

    private final EventAttendeesRepository eventAttendeesRepository;
    private final ActivityRepository activityRepository;
    private final ActivityAttendeesRepository activityAttendeesRepository;
    private final UserRepository userRepository;
    private final OrganizersRepository organizersRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Organizers getCurrentOrganizer() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return organizersRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));
    }

    // ORGANIZER CHECK-IN USER VÀO SỰ KIỆN ---
    @Override
    @Transactional
    public EventAttendeeResponseDTO organizerCheckInUser(String ticketCode) {
        EventAttendees ticket = eventAttendeesRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Vé không tồn tại hoặc mã không hợp lệ."));

        Organizers currentOrganizer = getCurrentOrganizer();
        if (!ticket.getEvent().getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền check-in cho sự kiện này.");
        }

        if (ticket.getStatus() != RegistrationStatus.APPROVED) {
            throw new IllegalArgumentException("Vé này chưa được duyệt hoặc đã bị từ chối.");
        }

        if (ticket.getEventCheckInStatus() == CheckInStatus.CHECKED_IN) {
            throw new IllegalArgumentException("Khách mời này đã check-in trước đó rồi.");
        }

        ticket.setEventCheckInStatus(CheckInStatus.CHECKED_IN);
        eventAttendeesRepository.save(ticket);
        return convertToAttendeeDTO(ticket);
    }

    // LOGIC 2: USER TỰ CHECK-IN VÀO ACTIVITY ---
    @Override
    @Transactional
    public String userCheckInActivity(String activityQrCode) {
        User currentUser = getCurrentUser();

        Activity activity = activityRepository.findByActivityQrCode(activityQrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Mã hoạt động không hợp lệ."));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime().minusMinutes(15))) {
            throw new IllegalArgumentException("Chưa đến giờ điểm danh cho hoạt động này.");
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new IllegalArgumentException("Hoạt động đã kết thúc, không thể điểm danh.");
        }

        ActivityAttendees activityAttendee = activityAttendeesRepository
                .findByActivity_ActivityIdAndEventAttendee_User_Id(activity.getActivityId(), currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa đăng ký tham gia hoạt động này."));

        if (activityAttendee.getEventAttendee().getEventCheckInStatus() != CheckInStatus.CHECKED_IN) {
             throw new IllegalArgumentException("Bạn chưa check-in vào cổng sự kiện.");
        }

        if (activityAttendee.getActCheckInStatus() == CheckInStatus.CHECKED_IN) {
            return "Bạn đã điểm danh hoạt động này rồi.";
        }

        activityAttendee.setActCheckInStatus(CheckInStatus.CHECKED_IN);
        activityAttendee.setCheckInTime(LocalDateTime.now());
        
        activityAttendeesRepository.save(activityAttendee);

        return "Điểm danh thành công hoạt động: " + activity.getActivityName();
    }
    
    private EventAttendeeResponseDTO convertToAttendeeDTO(EventAttendees entity) {
        return EventAttendeeResponseDTO.builder()
            .username(entity.getUser().getUsername())
            .email(entity.getUser().getEmail())
            .ticketCode(entity.getTicketCode())
            .eventCheckInStatus(entity.getEventCheckInStatus())
            .build();
    }
}