package com.example.backend.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body của POST /api/users/me/deletion.
 *
 * Mã sai định dạng (không đủ 6 chữ số) bị chặn ở tầng validation và trả 400
 * mà KHÔNG tính vào số lần nhập sai. Mã đúng định dạng nhưng sai giá trị mới
 * bị service tính lượt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAccountRequestDTO {

    @NotBlank(message = "Vui lòng nhập mã xác nhận.")
    @Pattern(regexp = "\\d{6}", message = "Mã xác nhận gồm đúng 6 chữ số.")
    private String otp;

    /** Lý do xoá tài khoản (tuỳ chọn). Chỉ ghi log, không lưu DB. */
    @Size(max = 500, message = "Lý do tối đa 500 ký tự.")
    private String reason;
}