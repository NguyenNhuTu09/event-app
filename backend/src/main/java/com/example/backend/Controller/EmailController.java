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
@Tag(name = "Gmail Management")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "Gửi Email cho người dùng")
    @PostMapping("/send-mail")
    public String sendMail(@RequestParam String toEmail) {
        String subject = "Test Email từ Spring Boot + Resend";
        String body = "Xin chào, đây là email được gửi qua Resend API!";
        return emailService.sendSimpleMail(toEmail, body, subject);
    }
}
