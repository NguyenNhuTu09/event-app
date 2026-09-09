package com.example.backend.DTO.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminSuspendUserRequestDTO {

    /** Số ngày cấm đăng khoảnh khắc, tính từ thời điểm gọi API. */
    @NotNull(message = "Số ngày là bắt buộc")
    @Min(value = 1, message = "Tối thiểu 1 ngày")
    @Max(value = 3650, message = "Tối đa 3650 ngày")
    private Integer days;

    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String reason;
}