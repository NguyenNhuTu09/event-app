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

import com.example.backend.DTO.Request.EventRegistrationRequestDTO;
import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventAttendeeResponseDTO;
import com.example.backend.DTO.Response.EventResponseDTO;
import com.example.backend.DTO.Response.UserRegistrationHistoryDTO;
import com.example.backend.Service.Interface.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events Management")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Lấy danh sách sự kiện công khai (Trang chủ)")
    @SecurityRequirements()
    @GetMapping("/public")
    public ResponseEntity<List<EventResponseDTO>> getPublicEvents() {
        return ResponseEntity.ok(eventService.getPublicEvents());
    }

    @Operation(summary = "Xem chi tiết sự kiện")
    @SecurityRequirements()
    @GetMapping("/{slug}") 
    public ResponseEntity<EventResponseDTO> getEventBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(eventService.getEventBySlug(slug));
    }

    @Operation(summary = "Tạo sự kiện mới (Chỉ ORGANIZER)")
    @PostMapping
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventRequestDTO requestDTO) {
        return new ResponseEntity<>(eventService.createEvent(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy danh sách sự kiện của tôi (Chỉ ORGANIZER)")
    @GetMapping("/my-events")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<List<EventResponseDTO>> getMyEvents() {
        return ResponseEntity.ok(eventService.getMyEvents());
    }

    @Operation(summary = "Cập nhật sự kiện (Chỉ ORGANIZER sở hữu)")
    @PutMapping("/{slug}")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable String slug, @Valid @RequestBody EventRequestDTO requestDTO) {
        return ResponseEntity.ok(eventService.updateEvent(slug, requestDTO));
    }

    @Operation(summary = "Xóa sự kiện (Chỉ ORGANIZER sở hữu)")
    @DeleteMapping("/{slug}")
    @PreAuthorize("hasAnyAuthority('ORGANIZER', 'SADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable String slug) {
        eventService.deleteEvent(slug);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Lấy tất cả sự kiện (SADMIN)")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }


    @Operation(summary = "Duyệt sự kiện (SADMIN)")
    @PutMapping("/{eventId}/approve")
    @PreAuthorize("hasAuthority('SADMIN')") 
    public ResponseEntity<EventResponseDTO> approveEvent(@PathVariable Long eventId) {
        EventResponseDTO approvedEvent = eventService.approveEvent(eventId);
        return ResponseEntity.ok(approvedEvent);
    }

    @Operation(summary = "Từ chối sự kiện (SADMIN)")
    @PutMapping("/{eventId}/reject")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<EventResponseDTO> rejectEvent(@PathVariable Long eventId, @RequestParam(required = false) String reason) {
        EventResponseDTO rejectedEvent = eventService.rejectEvent(eventId, reason);
        return ResponseEntity.ok(rejectedEvent);
    }

    @Operation(summary = "Đăng ký tham gia sự kiện (User)")
    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> registerForEvent(@RequestBody EventRegistrationRequestDTO requestDTO) {
        eventService.registerForEvent(requestDTO);
        return ResponseEntity.ok("Đăng ký thành công! Vui lòng chờ Organizer duyệt.");
    }

    @Operation(summary = "Lấy danh sách người đăng ký của Event (Organizer)")
    @GetMapping("/{eventId}/registrations")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<List<EventAttendeeResponseDTO>> getEventRegistrations(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventRegistrations(eventId));
    }

    @Operation(summary = "Duyệt vé cho người tham gia (Organizer)")
    @PutMapping("/registrations/{registrationId}/approve")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<String> approveRegistration(@PathVariable Long registrationId) {
        eventService.approveRegistration(registrationId);
        return ResponseEntity.ok("Đã duyệt vé thành công.");
    }

    @Operation(summary = "Từ chối vé (Organizer)")
    @PutMapping("/registrations/{registrationId}/reject")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<String> rejectRegistration(
            @PathVariable Long registrationId, 
            @RequestParam(required = false) String reason) { 
        
        String finalReason = (reason != null && !reason.isEmpty()) 
                            ? reason 
                            : "Đơn đăng ký không đáp ứng các tiêu chuẩn của ban tổ chức.";

        eventService.rejectRegistration(registrationId, finalReason);
        return ResponseEntity.ok("Đã từ chối vé.");
    }


    @Operation(summary = "Lấy danh sách 4 sự kiện nổi bật (User/Public)")
    @SecurityRequirements()
    @GetMapping("/featured")
    public ResponseEntity<List<EventResponseDTO>> getFeaturedEvents() {
        return ResponseEntity.ok(eventService.getFeaturedEvents());
    }

    @Operation(summary = "Lấy danh sách 8 sự kiện sắp diễn ra được chọn (User/Public)")
    @SecurityRequirements()
    @GetMapping("/upcoming-selected")
    public ResponseEntity<List<EventResponseDTO>> getUpcomingEvents() {
        return ResponseEntity.ok(eventService.getUpcomingEvents());
    }


    @Operation(summary = "Cập nhật danh sách sự kiện nổi bật (Max 4) - SADMIN")
    @PutMapping("/featured")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<EventResponseDTO>> updateFeaturedEvents(@RequestBody List<Long> eventIds) {
        return ResponseEntity.ok(eventService.updateFeaturedEvents(eventIds));
    }

    @Operation(summary = "Cập nhật danh sách sự kiện sắp diễn ra (Max 8) - SADMIN")
    @PutMapping("/upcoming-selected")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<EventResponseDTO>> updateUpcomingEvents(@RequestBody List<Long> eventIds) {
        return ResponseEntity.ok(eventService.updateUpcomingEvents(eventIds));
    }


    @Operation(summary = "Gửi yêu cầu duyệt sự kiện (Chỉ ORGANIZER sở hữu) - Chuyển từ DRAFT sang PENDING_APPROVAL")
    @PutMapping("/{slug}/submit")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> submitEventForApproval(@PathVariable String slug) {
        return ResponseEntity.ok(eventService.submitEventForApproval(slug));
    }

    @Operation(summary = "Xem lịch sử đăng ký sự kiện của tôi (User)")
    @GetMapping("/my-registrations")
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<List<UserRegistrationHistoryDTO>> getMyRegistrations() {
        return ResponseEntity.ok(eventService.getMyRegistrationHistory());
    }

    @Operation(summary = "Đăng ký/Hủy đăng ký nhận tin tức sự kiện hàng tuần")
    @PostMapping("/newsletter/subscribe")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> toggleNewsletter(@RequestParam boolean subscribe) {
        eventService.toggleNewsletterSubscription(subscribe);
        String message = subscribe ? "Đăng ký nhận tin thành công!" : "Đã hủy đăng ký nhận tin.";
        return ResponseEntity.ok(message);
    }
}