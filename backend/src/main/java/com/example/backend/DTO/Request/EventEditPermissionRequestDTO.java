package com.example.backend.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EventEditPermissionRequestDTO {
    @NotBlank(message = "Vui lòng nhập lý do cần chỉnh sửa")
    private String reason;
}