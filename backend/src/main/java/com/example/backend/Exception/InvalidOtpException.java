package com.example.backend.Exception;

/**
 * OTP sai, hết hạn, hoặc chưa được cấp.
 *
 * Kế thừa IllegalArgumentException để GlobalExceptionHandler trả 400 như các
 * lỗi nhập liệu khác. Tách thành lớp riêng để service có thể khai báo
 * noRollbackFor: số lần nhập sai phải được lưu lại dù request thất bại.
 */
public class InvalidOtpException extends IllegalArgumentException {
    public InvalidOtpException(String message) {
        super(message);
    }
}