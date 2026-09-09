package com.example.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ném ra khi một người dùng báo cáo cùng một moment lần thứ hai.
 *
 * @ResponseStatus khiến Spring tự trả 409 Conflict ngay cả khi dự án chưa có
 * GlobalExceptionHandler. Client mobile coi 409 là thành công và không hiện
 * lỗi cho người dùng.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateReportException extends RuntimeException {
    public DuplicateReportException(String message) {
        super(message);
    }
}