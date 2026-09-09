package com.example.backend.Models.Entity;

import java.time.LocalDateTime;

import com.example.backend.Utils.ReportReason;
import com.example.backend.Utils.ReportStatus;

import jakarta.persistence.*;
import lombok.*;

/**
 * Một lượt báo cáo nội dung của người dùng đối với một moment.
 *
 * Ràng buộc UNIQUE (moment_id, reporter_id) đảm bảo mỗi người chỉ báo cáo
 * một bài một lần — lần thứ hai service trả 409.
 *
 * Các cột momentOwnerId / momentImageUrl / momentCaption là bản chụp
 * (snapshot) tại thời điểm báo cáo. Chúng tồn tại vì job dọn dẹp xoá cứng
 * moment sau 3 ngày kể từ khi sự kiện kết thúc; nếu chỉ dựa vào khoá ngoại
 * thì hồ sơ báo cáo sẽ mất nội dung và không còn giá trị đối chiếu.
 */
@Entity
@Table(
    name = "moment_reports",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_moment_reporter",
        columnNames = {"moment_id", "reporter_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nullable: moment gốc có thể đã bị xoá bởi chủ bài hoặc bởi job dọn dẹp. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id")
    private EventMoment moment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private ReportReason reason;

    @Column(name = "detail", length = 500)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    // --- Snapshot ---

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "moment_owner_id")
    private Long momentOwnerId;

    @Column(name = "moment_image_url", length = 500)
    private String momentImageUrl;

    @Column(name = "moment_caption", columnDefinition = "TEXT")
    private String momentCaption;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
    }
}