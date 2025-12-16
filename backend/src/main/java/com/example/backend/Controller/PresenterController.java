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

import com.example.backend.DTO.Request.PresenterRequestDTO;
import com.example.backend.DTO.Response.PresenterResponseDTO;
import com.example.backend.Service.Interface.PresenterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/presenters")
@RequiredArgsConstructor
@Tag(name = "Presenters Management")
public class PresenterController {
    
    private final PresenterService presenterService;

    // --- PUBLIC ENDPOINTS (Ai cũng xem được) ---

    @Operation(summary = "Lấy danh sách tất cả diễn giả")
    @SecurityRequirements()
    @GetMapping
    public ResponseEntity<List<PresenterResponseDTO>> getAllPresenters() {
        return ResponseEntity.ok(presenterService.getAllPresenters());
    }

    @Operation(summary = "Xem chi tiết thông tin diễn giả")
    @SecurityRequirements()
    @GetMapping("/{presenterId}")
    public ResponseEntity<PresenterResponseDTO> getPresenterById(@PathVariable Integer presenterId) {
        return ResponseEntity.ok(presenterService.getPresenterById(presenterId));
    }

    @Operation(summary = "Tìm kiếm diễn giả (theo tên, công ty, chức danh)")
    @SecurityRequirements()
    @GetMapping("/search")
    public ResponseEntity<List<PresenterResponseDTO>> searchPresenters(@RequestParam String keyword) {
        return ResponseEntity.ok(presenterService.searchPresenters(keyword));
    }

    @Operation(summary = "Lấy danh sách diễn giả theo Sự kiện (Dựa vào lịch trình)")
    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<PresenterResponseDTO>> getPresentersByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(presenterService.getPresentersByEventId(eventId));
    }

    // --- MANAGEMENT ENDPOINTS (Cần quyền) ---

    @Operation(summary = "Tạo diễn giả mới (ORGANIZER hoặc SADMIN)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'SADMIN')") 
    public ResponseEntity<PresenterResponseDTO> createPresenter(@Valid @RequestBody PresenterRequestDTO requestDTO) {
        return new ResponseEntity<>(presenterService.createPresenter(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật thông tin diễn giả (ORGANIZER hoặc SADMIN)")
    @PutMapping("/{presenterId}")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'SADMIN')")
    public ResponseEntity<PresenterResponseDTO> updatePresenter(@PathVariable Integer presenterId, 
                                                                @Valid @RequestBody PresenterRequestDTO requestDTO) {
        return ResponseEntity.ok(presenterService.updatePresenter(presenterId, requestDTO));
    }

    @Operation(summary = "Xóa diễn giả (ORGANIZER hoặc SADMIN)")
    @DeleteMapping("/{presenterId}")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'SADMIN')")
    public ResponseEntity<Void> deletePresenter(@PathVariable Integer presenterId) {
        presenterService.deletePresenter(presenterId);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Kiểm tra diễn giả có bận trong khung giờ này không")
    @GetMapping("/check-availability")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'SADMIN')")
    public ResponseEntity<Boolean> checkPresenterAvailability(
            @RequestParam Integer presenterId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        boolean isBusy = presenterService.isPresenterBusy(presenterId, startTime, endTime);
        return ResponseEntity.ok(isBusy);
    }


    @Operation(summary = "Lấy danh sách diễn giả theo Nhà tổ chức (slug)")
    @SecurityRequirements() 
    @GetMapping("/by-organizer/{slug}")
    public ResponseEntity<List<PresenterResponseDTO>> getPresentersByOrganizer(@PathVariable String slug) {
        return ResponseEntity.ok(presenterService.getPresentersByOrganizerSlug(slug));
    }
}
