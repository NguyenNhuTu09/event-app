package com.example.backend.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptContentPolicyRequestDTO {

    /** Phiên bản Quy tắc cộng đồng người dùng vừa đọc và đồng ý, ví dụ "1.0". */
    @NotBlank(message = "Phiên bản quy tắc là bắt buộc")
    @Size(max = 20, message = "Phiên bản tối đa 20 ký tự")
    private String version;
}