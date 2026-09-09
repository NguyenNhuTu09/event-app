package com.example.backend.Models.Entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.Models.Gender;
import com.example.backend.Utils.AuthProvider;
import com.example.backend.Utils.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true, unique = true)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    private String address;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dateOfBirth;

    private String phoneNumber;

    private String avatarUrl;

    private String avatarS3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expiry_date")
    private Instant refreshTokenExpiryDate;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "token_expiry_date")
    private LocalDateTime tokenExpiryDate;

    @Column(nullable = false, unique = true, updatable = false)
    private String uid;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "is_subscribed_news")
    private boolean isSubscribedNews = false;

    // =================================================================
    // Kiểm duyệt nội dung do người dùng đăng (UGC)
    // Cột đã được tạo sẵn ở migration V2__moderation.sql
    // =================================================================

    /**
     * Thời điểm hết hạn cấm đăng khoảnh khắc. NULL = không bị cấm.
     * Đặt bởi POST /api/admin/users/{userId}/suspend.
     */
    @Column(name = "moment_suspended_until")
    private LocalDateTime momentSuspendedUntil;

    /** Phiên bản Quy tắc cộng đồng người dùng đã đồng ý (dùng ở bước 6). */
    @Column(name = "content_policy_accepted_version", length = 20)
    private String contentPolicyAcceptedVersion;

    /** Thời điểm đồng ý Quy tắc cộng đồng (dùng ở bước 6). */
    @Column(name = "content_policy_accepted_at")
    private LocalDateTime contentPolicyAcceptedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString();
        }
    }

    /** Tiện ích: user có đang bị cấm đăng bài tại thời điểm này không. */
    public boolean isMomentSuspended() {
        return momentSuspendedUntil != null && momentSuspendedUntil.isAfter(LocalDateTime.now());
    }
}