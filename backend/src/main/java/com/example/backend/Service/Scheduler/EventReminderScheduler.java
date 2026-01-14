package com.example.backend.Service.Scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Service.EmailService;
import com.example.backend.Utils.RegistrationStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventReminderScheduler {

    private final EventAttendeesRepository eventAttendeesRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 7 * * *") 
    @Transactional // Quan trọng: Giữ kết nối DB để lấy thông tin Lazy loading (User, Event)
    public void scanAndSendEventReminders() {
        System.out.println(">>> Bắt đầu Job quét sự kiện ngày mai...");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfDay = tomorrow.atStartOfDay(); // 00:00:00 ngày mai
        LocalDateTime endOfDay = tomorrow.atTime(LocalTime.MAX); // 23:59:59.999 ngày mai

        List<EventAttendees> attendees = eventAttendeesRepository.findAllApprovedAttendeesForDateRange(
                RegistrationStatus.APPROVED, 
                startOfDay, 
                endOfDay
        );

        if (attendees.isEmpty()) {
            System.out.println(">>> Không có sự kiện nào diễn ra vào ngày mai (" + tomorrow + ").");
            return;
        }

        System.out.println(">>> Tìm thấy " + attendees.size() + " người cần gửi nhắc nhở.");

        int successCount = 0;
        for (EventAttendees attendee : attendees) {
            try {
                emailService.sendEventReminderEmail(
                    attendee.getUser().getEmail(),
                    attendee.getUser().getUsername(),
                    attendee.getEvent().getEventName(),
                    attendee.getEvent().getStartDate(),
                    attendee.getEvent().getLocation(),
                    attendee.getTicketCode(),
                    attendee.getEvent().getSlug()
                );
                successCount++;

                Thread.sleep(100); 

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println(">>> Lỗi gửi cho User ID " + attendee.getUser().getId() + ": " + e.getMessage());
            }
        }

        System.out.println(">>> Kết thúc Job. Đã gửi thành công: " + successCount + "/" + attendees.size());
    }
}