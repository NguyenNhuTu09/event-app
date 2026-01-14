package com.example.backend.Service.Scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NewsletterScheduler {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * MON") 
    @Transactional
    public void sendWeeklyDigest() {
        System.out.println(">>> Bắt đầu Job gửi Newsletter hàng tuần...");

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Event> newEvents = eventRepository.findNewAndOpenEvents(sevenDaysAgo);

        if (newEvents.isEmpty()) {
            System.out.println(">>> Tuần này không có sự kiện mới. Bỏ qua việc gửi mail.");
            return;
        }

        List<User> subscribers = userRepository.findByIsSubscribedNewsTrue();

        if (subscribers.isEmpty()) {
            System.out.println(">>> Không có ai đăng ký nhận tin.");
            return;
        }

        System.out.println(">>> Tìm thấy " + newEvents.size() + " sự kiện mới và " + subscribers.size() + " người đăng ký.");

        
        for (User user : subscribers) {
            try {
                emailService.sendWeeklyNewsletter(user.getEmail(), newEvents);
                Thread.sleep(100); // Delay nhẹ tránh spam API
            } catch (Exception e) {
                System.err.println("Lỗi gửi newsletter: " + e.getMessage());
            }
        }
        
        System.out.println(">>> Hoàn tất gửi Newsletter.");
    }
}