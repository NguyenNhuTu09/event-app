package com.example.backend.Controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.AdminRemoveMomentRequestDTO;
import com.example.backend.DTO.Request.AdminSuspendUserRequestDTO;
import com.example.backend.DTO.Response.AdminReportResponseDTO;
import com.example.backend.Service.Interface.ModerationAdminService;
import com.example.backend.Utils.ReportStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Công cụ kiểm duyệt dành cho SADMIN.
 *
 * Không thuộc app mobile, nhưng Google sẽ hỏi app xử lý báo cáo ra sao —
 * đây là bằng chứng có quy trình thật. Có thể dùng trực tiếp qua Postman
 * hoặc để web admin gọi vào.
 *
 * Cam kết vận hành cần ghi trong Data safety: mọi báo cáo được xem xét
 * trong vòng 24 giờ. Trường hoursPending trong response giúp canh mốc này.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Moderation Admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('SADMIN')")
public class AdminModerationController {

    private final ModerationAdminService moderationAdminService;

    @Operation(summary = "Danh sách báo cáo nội dung (bỏ trống status để lấy tất cả)")
    @GetMapping("/reports")
    public ResponseEntity<?> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ReportStatus parsed = null;
        if (status != null && !status.isBlank()) {
            try {
                parsed = ReportStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Trạng thái không hợp lệ. Cho phép: PENDING, RESOLVED, DISMISSED."));
            }
        }

        // Báo cáo cũ nhất lên đầu: đó là cái sắp chạm mốc cam kết 24 giờ.
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        Page<AdminReportResponseDTO> result = moderationAdminService.getReports(parsed, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Gỡ một bài đăng vi phạm")
    @PostMapping("/moments/{momentId}/remove")
    public ResponseEntity<?> removeMoment(
            @PathVariable Long momentId,
            @Valid @RequestBody(required = false) AdminRemoveMomentRequestDTO request) {
        try {
            String note = (request == null) ? null : request.getNote();
            moderationAdminService.removeMoment(momentId, note);
            return ResponseEntity.ok(Map.of("momentId", momentId, "status", "REMOVED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Khôi phục một bài đăng đã bị ẩn hoặc gỡ")
    @PostMapping("/moments/{momentId}/restore")
    public ResponseEntity<?> restoreMoment(@PathVariable Long momentId) {
        try {
            moderationAdminService.restoreMoment(momentId);
            return ResponseEntity.ok(Map.of("momentId", momentId, "status", "VISIBLE"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Tạm khoá quyền đăng khoảnh khắc của một người dùng")
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminSuspendUserRequestDTO request) {
        try {
            moderationAdminService.suspendUser(userId, request.getDays(), request.getReason());
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "days", request.getDays()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}