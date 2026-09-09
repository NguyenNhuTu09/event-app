package com.example.backend.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Tách riêng khỏi EmailService để không phải sửa file đang có (EmailService
 * hiện đã hơn 400 dòng và được nhiều luồng khác dùng chung).
 *
 * Dùng lại đúng bean Resend và TemplateEngine đã cấu hình trong ResendConfig.
 *
 * Mọi phương thức đều nuốt lỗi: gửi mail hỏng không được phép làm rollback
 * hành động kiểm duyệt. Nhưng khác với EmailService đang dùng
 * e.printStackTrace(), ở đây ghi log ở mức error để còn truy vết được.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationEmailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    /** Báo cho chủ bài viết biết bài đã bị gỡ và vì sao. */
    public void sendMomentRemovedEmail(String to, String username, String eventName,
                                        String caption, String note) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("eventName", eventName);
            context.setVariable("caption", caption);
            context.setVariable("note", note);
            context.setVariable("removedAt", LocalDateTime.now().format(fullDateTimeFormatter));

            String html = templateEngine.process("moment-removed", context);

            resend.emails().send(CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Bài đăng của bạn đã bị gỡ - Webie Event")
                    .html(html)
                    .build());
        } catch (Exception e) {
            log.error("Không gửi được mail gỡ bài cho {}: {}", to, e.getMessage());
        }
    }

    /** Báo cho người dùng biết đang bị tạm khoá quyền đăng khoảnh khắc. */
    public void sendMomentSuspendedEmail(String to, String username,
                                          LocalDateTime until, String reason) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("until", until.format(fullDateTimeFormatter));
            context.setVariable("reason", reason);

            String html = templateEngine.process("moment-suspended", context);

            resend.emails().send(CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Tạm khoá quyền đăng nội dung - Webie Event")
                    .html(html)
                    .build());
        } catch (Exception e) {
            log.error("Không gửi được mail suspend cho {}: {}", to, e.getMessage());
        }
    }

    /**
     * Cảnh báo khẩn cho toàn bộ SADMIN khi có báo cáo CSAE.
     *
     * Google yêu cầu loại báo cáo này có đường xử lý riêng, không nằm chung
     * hàng đợi báo cáo thường. Đây là kênh tối thiểu để chứng minh điều đó.
     */
    public void sendCsaeAlert(List<String> adminEmails, Long momentId, Long eventId,
                              Long reporterId, Long ownerId, String imageUrl) {
        if (adminEmails == null || adminEmails.isEmpty()) {
            log.error("CSAE alert: không tìm thấy email SADMIN nào để gửi cảnh báo! momentId={}", momentId);
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("momentId", momentId);
            context.setVariable("eventId", eventId);
            context.setVariable("reporterId", reporterId);
            context.setVariable("ownerId", ownerId);
            context.setVariable("imageUrl", imageUrl);
            context.setVariable("reportedAt", LocalDateTime.now().format(fullDateTimeFormatter));

            String html = templateEngine.process("csae-alert", context);

            resend.emails().send(CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(adminEmails)
                    .subject("[KHẨN] Báo cáo CSAE cần xử lý ngay - Moment #" + momentId)
                    .html(html)
                    .build());
        } catch (Exception e) {
            log.error("Không gửi được cảnh báo CSAE cho momentId={}: {}", momentId, e.getMessage());
        }
    }
}