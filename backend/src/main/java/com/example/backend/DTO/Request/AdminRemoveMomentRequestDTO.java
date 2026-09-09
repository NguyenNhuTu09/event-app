package com.example.backend.DTO.Request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminRemoveMomentRequestDTO {

    /** Lý do gỡ — lưu vào resolution_note của các báo cáo liên quan
     *  và gửi cho chủ bài viết qua email. */
    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String note;
}