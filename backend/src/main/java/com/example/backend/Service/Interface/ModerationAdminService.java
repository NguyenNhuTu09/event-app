package com.example.backend.Service.Interface;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend.DTO.Response.AdminReportResponseDTO;
import com.example.backend.Utils.ReportStatus;

public interface ModerationAdminService {

    /** Danh sách báo cáo. status = null nghĩa là lấy tất cả. */
    Page<AdminReportResponseDTO> getReports(ReportStatus status, Pageable pageable);

    /** Gỡ bài: status = REMOVED, đóng báo cáo, đẩy WS DELETE, báo chủ bài. */
    void removeMoment(Long momentId, String note);

    /** Khôi phục bài: status = VISIBLE, bác các báo cáo liên quan, đẩy WS CREATE. */
    void restoreMoment(Long momentId);

    /** Cấm một người dùng đăng khoảnh khắc trong một số ngày. */
    void suspendUser(Long userId, int days, String reason);
}