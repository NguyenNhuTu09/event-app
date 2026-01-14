package com.example.backend.Controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

import com.example.backend.DTO.Request.MomentRequestDTO;
import com.example.backend.DTO.Response.MomentResponseDTO;
import com.example.backend.DTO.Response.SocketResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.ServiceImpl.EventMomentServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/{eventId}/moments")
@RequiredArgsConstructor
@Tag(name = "Event Moment Management")
public class EventMomentController {

    private final EventMomentServiceImpl momentService;
    private final UserRepository userRepository; 
    private final SimpMessagingTemplate messagingTemplate;

    @Operation(summary = "Đăng khoảnh khắc mới")
    @PostMapping
    public ResponseEntity<MomentResponseDTO> postMoment(
            @PathVariable Long eventId,
            @RequestBody MomentRequestDTO request) {
        Long userId = getCurrentUserId();
        MomentResponseDTO newMoment = momentService.createMoment(eventId, request, userId);

        SocketResponseDTO socketPayload = SocketResponseDTO.builder()
                .type("CREATE")
                .data(newMoment)
                .build();
        
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/moments", socketPayload);

        return ResponseEntity.ok(newMoment);
    }
    
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return user.getId();
    }

    @Operation(summary = "Lấy danh sách khoảnh khắc (Có phân trang)")
    @GetMapping
    public ResponseEntity<Page<MomentResponseDTO>> getMoments(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageable = PageRequest.of(page, size, Sort.by("postedAt").descending());
        return ResponseEntity.ok(momentService.getEventMoments(eventId, pageable));
    }

    @Operation(summary = "Xem khoảnh khắc của tôi tại sự kiện này")
    @GetMapping("/me")
    public ResponseEntity<List<MomentResponseDTO>> getMyMoments(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(momentService.getMyMoments(eventId, userId));
    }

    @Operation(summary = "Xóa khoảnh khắc của mình")
    @DeleteMapping("/{momentId}")
    public ResponseEntity<Void> deleteMoment(
            @PathVariable Long eventId,
            @PathVariable Long momentId) {
        Long userId = getCurrentUserId();
        momentService.deleteMoment(eventId, momentId, userId);

        SocketResponseDTO socketPayload = SocketResponseDTO.builder()
                .type("DELETE")
                .data(momentId) 
                .build();

        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/moments", socketPayload);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Sửa nội dung khoảnh khắc")
    @PutMapping("/{momentId}")
    public ResponseEntity<MomentResponseDTO> updateMoment(
            @PathVariable Long eventId,
            @PathVariable Long momentId,
            @RequestBody MomentRequestDTO request) {
        Long userId = getCurrentUserId();
        MomentResponseDTO updatedMoment = momentService.updateMoment(eventId, momentId, request, userId);

        SocketResponseDTO socketPayload = SocketResponseDTO.builder()
                .type("UPDATE")
                .data(updatedMoment)
                .build();

        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/moments", socketPayload);

        return ResponseEntity.ok(updatedMoment);
    }
}
