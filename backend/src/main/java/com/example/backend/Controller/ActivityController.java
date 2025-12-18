package com.example.backend.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.ActivityRequestDTO;
import com.example.backend.DTO.Response.ActivityResponseDTO;
import com.example.backend.Service.Interface.ActivityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Tag(name = "Activities Management")
public class ActivityController {
    private final ActivityService activityService;
    // --- PUBLIC ENDPOINTS (Cho người tham gia xem) ---

    @Operation(summary = "Xem chi tiết một hoạt động")
    @SecurityRequirements()
    @GetMapping("/{activityId}")
    public ResponseEntity<ActivityResponseDTO> getActivityById(@PathVariable Integer activityId) {
        return ResponseEntity.ok(activityService.getActivityById(activityId));
    }

    @Operation(summary = "Lấy toàn bộ lịch trình của một sự kiện (Agenda)")
    @SecurityRequirements()
    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<ActivityResponseDTO>> getActivitiesByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(activityService.getActivitiesByEventId(eventId));
    }

    @Operation(summary = "Lấy danh sách hoạt động của một diễn giả")
    @GetMapping("/by-presenter/{presenterId}")
    public ResponseEntity<List<ActivityResponseDTO>> getActivitiesByPresenter(@PathVariable Integer presenterId) {
        return ResponseEntity.ok(activityService.getActivitiesByPresenterId(presenterId));
    }

    @Operation(summary = "Tìm kiếm hoạt động trong sự kiện (Theo tên, mô tả)")
    @SecurityRequirements()
    @GetMapping("/search")
    public ResponseEntity<List<ActivityResponseDTO>> searchActivities(@RequestParam Long eventId, 
                                                                      @RequestParam String keyword) {
        return ResponseEntity.ok(activityService.searchActivitiesInEvent(eventId, keyword));
    }

    // --- MANAGEMENT ENDPOINTS (Cần quyền) ---

    @Operation(summary = "Tạo hoạt động mới cho sự kiện (SADMIN, ORGANIZER)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SADMIN', 'ORGANIZER')")
    public ResponseEntity<ActivityResponseDTO> createActivity(@Valid @RequestBody ActivityRequestDTO requestDTO) {
        return new ResponseEntity<>(activityService.createActivity(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật thông tin hoạt động (SADMIN, ORGANIZER)")
    @PutMapping("/{activityId}")
    @PreAuthorize("hasAnyAuthority('SADMIN', 'ORGANIZER')")
    public ResponseEntity<ActivityResponseDTO> updateActivity(@PathVariable Integer activityId, 
                                                              @Valid @RequestBody ActivityRequestDTO requestDTO) {
        return ResponseEntity.ok(activityService.updateActivity(activityId, requestDTO));
    }

    @Operation(summary = "Xóa hoạt động (SADMIN, ORGANIZER)")
    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasAnyAuthority('SADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteActivity(@PathVariable Integer activityId) {
        activityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

}
