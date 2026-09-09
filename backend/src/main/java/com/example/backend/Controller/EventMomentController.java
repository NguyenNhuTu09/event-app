package com.example.backend.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.MomentReportRequestDTO;
import com.example.backend.DTO.Request.MomentRequestDTO;
import com.example.backend.DTO.Response.MomentResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.ServiceImpl.EventMomentServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Toàn bộ việc phát WebSocket đã được chuyển xuống service layer, để phần
 * kiểm duyệt (auto-hide, admin gỡ bài ở bước 5) cũng phát được sự kiện mà
 * không phải đi vòng qua controller.
 */
@RestController
@RequestMapping("/api/events/{eventId}/moments")
@RequiredArgsConstructor
@Tag(name = "Event Moment Management")
public class EventMomentController {

    private final EventMomentServiceImpl momentService;
    private final UserRepository userRepository;

    @Operation(summary = "Đăng khoảnh khắc mới")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MomentResponseDTO> postMoment(
            @PathVariable Long eventId,
            @RequestBody MomentRequestDTO request) {
        return ResponseEntity.ok(momentService.createMoment(eventId, request, getCurrentUserId()));
    }

    @Operation(summary = "Lấy danh sách khoảnh khắc (đã lọc kiểm duyệt & danh sách chặn)")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<MomentResponseDTO>> getMoments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("postedAt").descending());
        return ResponseEntity.ok(momentService.getEventMoments(eventId, getCurrentUserId(), pageable));
    }

    @Operation(summary = "Xem khoảnh khắc của tôi tại sự kiện này")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MomentResponseDTO>> getMyMoments(@PathVariable Long eventId) {
        return ResponseEntity.ok(momentService.getMyMoments(eventId, getCurrentUserId()));
    }

    @Operation(summary = "Sửa nội dung khoảnh khắc")
    @PutMapping("/{momentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MomentResponseDTO> updateMoment(
            @PathVariable Long eventId,
            @PathVariable Long momentId,
            @RequestBody MomentRequestDTO request) {
        return ResponseEntity.ok(momentService.updateMoment(eventId, momentId, request, getCurrentUserId()));
    }

    @Operation(summary = "Xóa khoảnh khắc của mình")
    @DeleteMapping("/{momentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteMoment(
            @PathVariable Long eventId,
            @PathVariable Long momentId) {
        momentService.deleteMoment(eventId, momentId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Báo cáo một khoảnh khắc vi phạm")
    @PostMapping("/{momentId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reportMoment(
            @PathVariable Long eventId,
            @PathVariable Long momentId,
            @Valid @RequestBody MomentReportRequestDTO request) {
        try {
            return ResponseEntity.ok(
                    momentService.reportMoment(eventId, momentId, request, getCurrentUserId()));
        } catch (IllegalArgumentException e) {
            // Báo cáo bài của chính mình -> 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
        // DuplicateReportException đã gắn @ResponseStatus(CONFLICT) -> Spring tự trả 409.
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}