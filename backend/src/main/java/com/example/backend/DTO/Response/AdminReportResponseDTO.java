package com.example.backend.DTO.Response;

import java.time.LocalDateTime;

import com.example.backend.Utils.MomentStatus;
import com.example.backend.Utils.ReportReason;
import com.example.backend.Utils.ReportStatus;

import lombok.Builder;
import lombok.Data;

/**
 * Một dòng trong GET /api/admin/reports.
 *
 * Chứa đủ thông tin để admin ra quyết định mà không phải gọi thêm API:
 * nội dung bị báo cáo, người đăng, người báo cáo, lý do.
 *
 * Khi moment gốc đã bị xoá (chủ bài tự xoá hoặc job dọn dẹp sau 3 ngày),
 * momentId = null và momentDeleted = true, nhưng ảnh/caption vẫn còn nhờ
 * các cột snapshot trong bảng moment_reports.
 */
@Data
@Builder
public class AdminReportResponseDTO {

    // --- Bản thân báo cáo ---
    private Long id;
    private ReportReason reason;
    private String detail;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String resolutionNote;
    private String resolvedByUsername;

    /** Số giờ báo cáo đã nằm chờ — dùng để canh cam kết xử lý trong 24h. */
    private Long hoursPending;

    // --- Nội dung bị báo cáo ---
    private Long eventId;
    private Long momentId;
    private MomentStatus momentStatus;
    private String momentImageUrl;
    private String momentCaption;
    private boolean momentDeleted;

    /** Tổng số báo cáo còn hiệu lực trên cùng moment này. */
    private Long totalReportsOnMoment;

    // --- Người đăng ---
    private Long ownerId;
    private String ownerUsername;
    private String ownerEmail;
    private LocalDateTime ownerSuspendedUntil;

    // --- Người báo cáo ---
    private Long reporterId;
    private String reporterUsername;
    private String reporterEmail;
}