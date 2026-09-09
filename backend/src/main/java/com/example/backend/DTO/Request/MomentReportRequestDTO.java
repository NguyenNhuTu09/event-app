package com.example.backend.DTO.Request;

import com.example.backend.Utils.ReportReason;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MomentReportRequestDTO {

    @NotNull(message = "Lý do báo cáo là bắt buộc")
    private ReportReason reason;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String detail;
}