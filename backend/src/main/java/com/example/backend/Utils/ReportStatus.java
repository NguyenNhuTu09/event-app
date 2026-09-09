package com.example.backend.Utils;

/**
 * Vòng đời của một báo cáo.
 *
 * PENDING   - chờ admin xử lý (cam kết với Google: trong vòng 24 giờ).
 * RESOLVED  - admin đã xác nhận vi phạm và xử lý (gỡ bài / khoá user).
 * DISMISSED - admin xác định không vi phạm, bỏ qua.
 *             Báo cáo DISMISSED không được tính vào ngưỡng tự động ẩn.
 */
public enum ReportStatus {
    PENDING,
    RESOLVED,
    DISMISSED
}