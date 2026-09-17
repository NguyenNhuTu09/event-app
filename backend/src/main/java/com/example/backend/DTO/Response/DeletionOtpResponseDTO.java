package com.example.backend.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response của POST /api/users/me/deletion-otp.
 *
 * Hai trường số giây để client hiển thị đếm ngược mà không phải tự đoán
 * cấu hình phía server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletionOtpResponseDTO {

    private String message;

    /** Số giây tới khi mã hết hạn. */
    private long expiresInSeconds;

    /** Số giây tới khi được phép xin mã mới (nút "Gửi lại mã"). */
    private long resendAfterSeconds;
}