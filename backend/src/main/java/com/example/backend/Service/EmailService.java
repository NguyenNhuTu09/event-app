package com.example.backend.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.backend.DTO.ActivityEmailDTO;
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

    private final DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

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

    public void sendRegistrationPendingEmail(String to, String username, String eventName, 
                                         LocalDateTime startDateTime, LocalDateTime endDateTime,
                                         String location) {
        try {
            String startStr = (startDateTime != null) ? startDateTime.format(fullDateTimeFormatter) : "";
            String endStr = (endDateTime != null) ? endDateTime.format(fullDateTimeFormatter) : "";

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            
            context.setVariable("eventStartFull", startStr);
            context.setVariable("eventEndFull", endStr);
            
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
                                              LocalDateTime eventStartDateTime, 
                                              LocalDateTime eventEndDateTime, 
                                              String location, 
                                              String ticketCode, 
                                              List<ActivityEmailDTO> activityList) {
        try {
            String startStr = (eventStartDateTime != null) ? eventStartDateTime.format(fullDateTimeFormatter) : "";
            String endStr = (eventEndDateTime != null) ? eventEndDateTime.format(fullDateTimeFormatter) : "";

            String qrCodeUrl = "";
            if (ticketCode != null && !ticketCode.isEmpty()) {
                try {
                    String encodedTicket = URLEncoder.encode(ticketCode, StandardCharsets.UTF_8);
                    qrCodeUrl = "https://quickchart.io/qr?text=" + encodedTicket + "&size=300&margin=1";
                } catch (Exception e) {
                    e.printStackTrace();
                    qrCodeUrl = "https://quickchart.io/qr?text=" + ticketCode + "&size=300&margin=1";
                }
            }

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            
            context.setVariable("eventStartFull", startStr);
            context.setVariable("eventEndFull", endStr);
            
            context.setVariable("location", location);
            context.setVariable("ticketCode", ticketCode);
            context.setVariable("qrCodeUrl", qrCodeUrl);
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

    public void sendRegistrationRejectedEmail(String to, String username, String eventName, 
                                          LocalDateTime startDateTime, String location, String reason) {
        try {
            String startStr = (startDateTime != null) ? startDateTime.format(fullDateTimeFormatter) : "";

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("eventStartFull", startStr);
            context.setVariable("location", location);
            context.setVariable("reason", reason); // Có thể null

            String htmlContent = templateEngine.process("event-registration-rejected", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Thông báo kết quả đăng ký: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
            System.out.println("Registration Rejected Email sent to " + to);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}