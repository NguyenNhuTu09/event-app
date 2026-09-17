package com.example.backend.Service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.backend.Utils.AppTime;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Email cho luồng xoá tài khoản. Tách khỏi EmailService theo cùng lý do với
 * ModerationEmailService: không phải sửa file đang được nhiều luồng dùng chung.
 *
 * Mọi phương thức đều nuốt lỗi và ghi log error.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEmailService {

    private final Resend resend;
    private final TemplateEngine templateEngine;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    /** Mã OTP xác nhận xoá tài khoản. */
    public void sendDeletionOtpEmail(String to, String username, String otp, long ttlMinutes) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("otpCode", otp);
            context.setVariable("ttlMinutes", ttlMinutes);

            String html = templateEngine.process("account-deletion-otp", context);

            resend.emails().send(CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Mã xác nhận xoá tài khoản - Webie Event")
                    .html(html)
                    .build());
        } catch (Exception e) {
            log.error("Không gửi được mail OTP xoá tài khoản cho {}: {}", to, e.getMessage());
        }
    }

    /** Xác nhận tài khoản đã bị xoá (do chính người dùng hoặc do quản trị viên). */
    public void sendAccountDeletedEmail(String to, String username, boolean deletedByAdmin) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("deletedByAdmin", deletedByAdmin);
            // AppTime: mốc chỉ để hiển thị cho người đọc ở VN
            context.setVariable("deletedAt", AppTime.now().format(fullDateTimeFormatter));

            String html = templateEngine.process("account-deleted", context);

            resend.emails().send(CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject("Tài khoản của bạn đã được xoá - Webie Event")
                    .html(html)
                    .build());
        } catch (Exception e) {
            log.error("Không gửi được mail xác nhận xoá tài khoản cho {}: {}", to, e.getMessage());
        }
    }
}