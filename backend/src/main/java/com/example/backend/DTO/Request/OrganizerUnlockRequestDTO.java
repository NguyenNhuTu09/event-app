package com.example.backend.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrganizerUnlockRequestDTO {
    @NotBlank(message = "Lý do yêu cầu mở khóa không được để trống")
    private String reason;
}