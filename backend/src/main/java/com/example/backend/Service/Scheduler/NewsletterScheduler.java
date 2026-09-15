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

    /**
     * Chạy 8h sáng thứ Hai giờ Việt Nam.
     *
     * Thiếu thuộc tính zone thì cron chạy theo múi giờ JVM — hiện là UTC, nên
     * job vốn bắn lúc 15h chiều thứ Hai giờ VN.
     */
    @Scheduled(cron = "0 0 8 * * MON", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void sendWeeklyDigest() {
        System.out.println(">>> Bắt đầu Job gửi Newsletter hàng tuần...");

        // GIỮ NGUYÊN LocalDateTime.now(): mốc này được đem so với e.createdAt
        // trong findNewAndOpenEvents, mà createdAt do server sinh ra nên cũng
        // đang là giờ UTC. Hai vế cùng hệ. Đổi sang AppTime.now() sẽ làm cửa
        // sổ 7 ngày bị xê dịch.
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