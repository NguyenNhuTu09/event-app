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

import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventResponseDTO;
import com.example.backend.Service.Interface.EventService;

import io.swagger.v3.oas.annotations.Operation;
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
    @GetMapping("/public")
    public ResponseEntity<List<EventResponseDTO>> getPublicEvents() {
        return ResponseEntity.ok(eventService.getPublicEvents());
    }

    @Operation(summary = "Xem chi tiết sự kiện")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
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
    @PutMapping("/{eventId}")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<EventResponseDTO> updateEvent(@PathVariable Long eventId, @Valid @RequestBody EventRequestDTO requestDTO) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, requestDTO));
    }

    @Operation(summary = "Xóa sự kiện (Chỉ ORGANIZER sở hữu)")
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
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
}