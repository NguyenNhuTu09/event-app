package com.example.backend.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.Service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@Tag(name = "Mail Management")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "Gửi Email cho người dùng")
    @PostMapping("/send-mail")
    public String sendMail(@RequestParam String toEmail) {
        String subject = "Test Email từ Spring Boot";
        String body = "Xin chào, đây là email được gửi tự động từ ứng dụng Spring Boot!";
        return emailService.sendSimpleMail(toEmail, body, subject);
    }
}
