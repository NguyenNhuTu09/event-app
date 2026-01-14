package com.example.backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Service.Scheduler.NewsletterScheduler;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class NewsletterTestController {
    private final NewsletterScheduler newsletterScheduler;

    @PostMapping("/trigger-newsletter")
    public ResponseEntity<String> triggerNewsletterManual() {
        newsletterScheduler.sendWeeklyDigest();
        return ResponseEntity.ok("Đã kích hoạt gửi Newsletter! Hãy kiểm tra Console và Email.");
    }
}
