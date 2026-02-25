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
import com.example.backend.Models.Entity.Event;
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

    public void sendVerificationEmail(String to, String username, String otpCode) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("otpCode", otpCode); // Truyền mã OTP

            String htmlContent = templateEngine.process("verify-account", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Mã xác thực tài khoản Webie Event")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
            System.out.println("Verification OTP sent to " + to);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEventSubmissionPending(String to, String username, String eventName, LocalDateTime submittedDate) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("submittedDate", submittedDate.format(fullDateTimeFormatter));

            String htmlContent = templateEngine.process("event-submission-pending", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Xác nhận gửi yêu cầu duyệt: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error sending Pending email: " + e.getMessage());
        }
    }


    public void sendEventApprovedEmail(String to, String username, String eventName, LocalDateTime eventStartDate, String eventSlug) {
        try {
            // Giả lập đường dẫn FE,sau này thay bằng domain thật
            String eventLink = "http://localhost:3000/events/" + eventSlug; 

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("eventDate", eventStartDate.format(fullDateTimeFormatter));
            context.setVariable("eventLink", eventLink);

            String htmlContent = templateEngine.process("event-submission-approved", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Sự kiện của bạn đã được duyệt: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEventRejectedEmail(String to, String username, String eventName, String reason) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("reason", reason);

            String htmlContent = templateEngine.process("event-submission-rejected", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Thông báo từ chối sự kiện: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void sendOrganizerRegistrationPending(String to, String username, String organizerName) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("organizerName", organizerName);

            String htmlContent = templateEngine.process("organizer-pending", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Xác nhận đăng ký Organizer: " + organizerName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOrganizerApproved(String to, String username, String organizerName) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("organizerName", organizerName);

            String htmlContent = templateEngine.process("organizer-approved", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Chúc mừng! Bạn đã trở thành Organizer")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOrganizerRejected(String to, String username, String organizerName, String reason) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("organizerName", organizerName);
            context.setVariable("reason", reason);

            String htmlContent = templateEngine.process("organizer-rejected", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Thông báo kết quả đăng ký Organizer")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendEventReminderEmail(String to, String username, String eventName, 
                                    LocalDateTime startDate, String location, 
                                    String ticketCode, String eventSlug) {
        try {
            String timeStr = startDate.format(fullDateTimeFormatter);
            String eventLink = "https://ems.webie.com.vn/event/" + eventSlug; 

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("startTime", timeStr);
            context.setVariable("location", location);
            context.setVariable("ticketCode", ticketCode);
            context.setVariable("eventLink", eventLink);

            String htmlContent = templateEngine.process("event-reminder", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Nhắc nhở: Sự kiện " + eventName + " diễn ra vào ngày mai!")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
            System.out.println("Reminder email sent to " + to);
        } catch (Exception e) {
            System.err.println("Error sending reminder to " + to + ": " + e.getMessage());
        }
    }

    public void sendWeeklyNewsletter(String to, List<Event> newEvents) {
        try {
            Context context = new Context();
            context.setVariable("events", newEvents);

            String htmlContent = templateEngine.process("weekly-newsletter", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("🔥 Tổng hợp sự kiện mới tuần qua - Webie Event")
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            System.err.println("Lỗi gửi Newsletter cho " + to + ": " + e.getMessage());
        }
    }

    public void sendEditRequestPendingEmail(String to, String username, String eventName, String reason) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("reason", reason);
            context.setVariable("submittedDate", LocalDateTime.now().format(fullDateTimeFormatter));

            String htmlContent = templateEngine.process("event-edit-request-pending", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Đã tiếp nhận yêu cầu chỉnh sửa: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEditRequestApprovedEmail(String to, String username, String eventName, String eventSlug) {
        try {
            // Link dẫn đến trang quản lý sự kiện của Organizer để họ sửa
            String editLink = "http://localhost:3000/organizer/events/" + eventSlug + "/edit";

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("editLink", editLink);

            String htmlContent = templateEngine.process("event-edit-request-approved", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Yêu cầu chỉnh sửa được CHẤP THUẬN: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendEditRequestRejectedEmail(String to, String username, String eventName, String reason) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("reason", reason);

            String htmlContent = templateEngine.process("event-edit-request-rejected", context);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Yêu cầu chỉnh sửa BỊ TỪ CHỐI: " + eventName)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}