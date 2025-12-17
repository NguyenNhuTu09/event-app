package com.example.backend.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;

    @Value("${resend.from.email}")
    private String fromEmail;

    public String sendSimpleMail(String recipient, String msgBody, String subject) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(recipient)
                    .subject(subject)
                    .text(msgBody)
                    .build();

            CreateEmailResponse data = resend.emails().send(params);
            return "Gửi mail thành công! ID: " + data.getId();
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gửi mail: " + e.getMessage();
        }
    }

    public void sendEmailWithTemplate(String to, String subject, String username, String otp) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("otpCode", otp);

            String htmlContent = templateEngine.process("otp-email", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlContent) 
                    .build();

            resend.emails().send(params);
            System.out.println("OTP Email sent successfully to " + to);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error sending OTP email: " + e.getMessage());
        }
    }

    public void sendNotificationEmail(String to, String subject, String username) {
        try {
            Context context = new Context();
            context.setVariable("username", username);

            String htmlContent = templateEngine.process("password-changed", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
            System.out.println("Notification Email sent successfully to " + to);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error sending notification email: " + e.getMessage());
        }
    }

    public void sendRegistrationPendingEmail(String to, String username, String eventName, String startDate, String location) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("startDate", startDate);
            context.setVariable("location", location);

            String htmlContent = templateEngine.process("event-registration-pending", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Xác nhận đăng ký: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

    public void sendRegistrationApprovedEmail(String to, String username, String eventName, 
                                              String startDate, String location, 
                                              String ticketCode, List<String> activityList) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("startDate", startDate);
            context.setVariable("location", location);
            context.setVariable("ticketCode", ticketCode);
            context.setVariable("activityList", activityList);

            String htmlContent = templateEngine.process("event-registration-approved", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Vé tham dự sự kiện: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}