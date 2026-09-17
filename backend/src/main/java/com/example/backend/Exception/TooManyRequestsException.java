package com.example.backend.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Người dùng thao tác quá nhanh hoặc quá nhiều lần (xin OTP trong thời gian
 * chờ, nhập sai OTP quá số lần cho phép). Client nên hiển thị message và
 * không tự động gửi lại.
 */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}