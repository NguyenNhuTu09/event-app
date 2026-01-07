package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.MomentRequestDTO;
import com.example.backend.DTO.Response.MomentResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.EventMoment;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Repository.EventMomentRepository;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Utils.CheckInStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventMomentServiceImpl {
    private final EventMomentRepository momentRepository;
    private final EventAttendeesRepository eventAttendeesRepository;
    private final EventRepository eventRepository;
    private static final int DAYS_TO_KEEP_MOMENTS = 3;

    public MomentResponseDTO createMoment(Long eventId, MomentRequestDTO request, Long userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (LocalDateTime.now().isAfter(event.getEndDate().plusDays(DAYS_TO_KEEP_MOMENTS))) {
            throw new IllegalArgumentException("Sự kiện đã kết thúc quá 3 ngày, tính năng này đã đóng.");
        }

        EventAttendees attendee = eventAttendeesRepository.findByEvent_EventIdAndUser_Id(eventId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Bạn chưa đăng ký tham gia sự kiện này."));

        if (attendee.getEventCheckInStatus() != CheckInStatus.CHECKED_IN) {
            throw new IllegalArgumentException("Bạn cần check-in tại sự kiện để sử dụng tính năng này.");
        }
        User user = attendee.getUser(); 

        EventMoment moment = EventMoment.builder()
                .event(event)
                .user(user)
                .caption(request.getCaption())
                .imageUrl(request.getImageUrl())
                .build();

        EventMoment saved = momentRepository.save(moment);
        return mapToDTO(saved);
    }

    public List<MomentResponseDTO> getEventMoments(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (LocalDateTime.now().isAfter(event.getEndDate().plusDays(DAYS_TO_KEEP_MOMENTS))) {
            return Collections.emptyList();
        }
        return momentRepository.findByEventIdWithUser(eventId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public Page<MomentResponseDTO> getEventMoments(Long eventId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
             .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (LocalDateTime.now().isAfter(event.getEndDate().plusDays(3))) {
            return Page.empty();
        }

        return momentRepository.findByEventIdWithUser(eventId, pageable)
                .map(this::mapToDTO);
    }

    @Scheduled(cron = "0 0 2 * * ?") 
    @Transactional
    public void cleanupExpiredMoments() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(DAYS_TO_KEEP_MOMENTS);
        
        List<EventMoment> expiredMoments = momentRepository.findExpiredMoments(threshold);
        
        if (!expiredMoments.isEmpty()) {
            List<Long> ids = expiredMoments.stream().map(EventMoment::getId).collect(Collectors.toList());
            momentRepository.deleteByIds(ids);
            System.out.println("Đã xóa " + ids.size() + " khoảnh khắc hết hạn.");
        }
    }

    private MomentResponseDTO mapToDTO(EventMoment entity) {
        if (entity == null) {
            return null;
        }
        Long userId = entity.getUser() != null ? entity.getUser().getId() : null;
        String username = entity.getUser() != null ? entity.getUser().getUsername() : "Unknown";
        String avatarUrl = entity.getUser() != null ? entity.getUser().getAvatarUrl() : null;

        return MomentResponseDTO.builder()
                .id(entity.getId())
                .userId(userId)
                .username(username)
                .userAvatar(avatarUrl)
                .caption(entity.getCaption())
                .imageUrl(entity.getImageUrl())
                .postedAt(entity.getPostedAt())
                .timeAgo(calculateTimeAgo(entity.getPostedAt())) 
                .build();
    }

    private String calculateTimeAgo(LocalDateTime postedAt) {
        if (postedAt == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(postedAt, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "Vừa xong";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " phút trước";
        } else if (seconds < 86400) { // 24 giờ * 60 phút * 60 giây
            long hours = seconds / 3600;
            return hours + " giờ trước";
        } else if (seconds < 259200) { // 3 ngày
            long days = seconds / 86400;
            return days + " ngày trước";
        } else {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return postedAt.format(formatter);
        }
    }


    public void deleteMoment(Long eventId, Long momentId, Long userId) {
        EventMoment moment = momentRepository.findByIdAndUser_Id(momentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết hoặc bạn không có quyền xóa."));
        momentRepository.delete(moment);
    }

    public MomentResponseDTO updateMoment(Long eventId, Long momentId, MomentRequestDTO request, Long userId) {
        EventMoment moment = momentRepository.findByIdAndUser_Id(momentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết hoặc bạn không có quyền sửa."));

        moment.setCaption(request.getCaption());
        
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            moment.setImageUrl(request.getImageUrl());
        }

        EventMoment updated = momentRepository.save(moment);
        return mapToDTO(updated);
    }
    
    public List<MomentResponseDTO> getMyMoments(Long eventId, Long userId) {
        return momentRepository.findByEventIdAndUserId(eventId, userId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

}
