package com.example.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ném ra khi người dùng đã đăng nhập nhưng không có quyền trên tài nguyên cụ thể
 * — khác với việc thiếu vai trò, vốn đã được @PreAuthorize chặn từ trước.
 *
 * Ví dụ: organizer quét vé của sự kiện do organizer khác tổ chức. Trước đây chỗ
 * này ném RuntimeException nên Spring trả 500, khiến client phải dò chuỗi
 * "không có quyền check-in" để phân biệt với lỗi hệ thống thật.
 *
 * @ResponseStatus khiến Spring trả 403 ngay cả khi dự án chưa có
 * GlobalExceptionHandler.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}