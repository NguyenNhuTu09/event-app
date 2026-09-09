package com.example.backend.DTO.Response;

import com.example.backend.Utils.ReportStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MomentReportResponseDTO {
    private Long id;
    private ReportStatus status;
}