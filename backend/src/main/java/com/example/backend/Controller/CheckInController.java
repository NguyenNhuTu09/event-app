package com.example.backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.CheckInRequestDTO;
import com.example.backend.DTO.Response.EventAttendeeResponseDTO;
import com.example.backend.Service.Interface.CheckInService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
@Tag(name = "Check-In Management")
public class CheckInController {

    private final CheckInService checkInService;

    @Operation(summary = "Organizer quét mã vé của User (Cổng sự kiện)")
    @PostMapping("/event")
    @PreAuthorize("hasAuthority('ORGANIZER')")
    public ResponseEntity<EventAttendeeResponseDTO> checkInEvent(@RequestBody CheckInRequestDTO request) {
        return ResponseEntity.ok(checkInService.organizerCheckInUser(request.getTicketCode()));
    }

    @Operation(summary = "User quét mã Activity (Tại phòng họp)")
    @PostMapping("/activity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> checkInActivity(@RequestBody CheckInRequestDTO request) {
        return ResponseEntity.ok(checkInService.userCheckInActivity(request.getActivityQrCode()));
    }
}