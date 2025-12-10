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
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.OrganizersRequestDTO;
import com.example.backend.DTO.Response.OrganizersResponseDTO;
import com.example.backend.Service.Interface.OrganizersService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizers")
@RequiredArgsConstructor
@Tag(name = "Organizers Management") 
@SecurityRequirement(name = "bearerAuth")
public class OrganizersController {

    private final OrganizersService organizersService;

    @Operation(summary = "Đăng ký làm nhà tổ chức sự kiện (User hiện tại)")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizersResponseDTO> createOrganizer(@Valid @RequestBody OrganizersRequestDTO requestDTO) {
        OrganizersResponseDTO createdOrganizer = organizersService.createOrganizer(requestDTO);
        return new ResponseEntity<>(createdOrganizer, HttpStatus.CREATED);
    }
   
    @Operation(summary = "Phê duyệt đăng ký Organizer (SADMIN only)")
    @PutMapping("/{organizerId}/approve")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<OrganizersResponseDTO> approveOrganizer(@PathVariable Integer organizerId) {
        OrganizersResponseDTO approvedOrganizer = organizersService.approveOrganizer(organizerId);
        return ResponseEntity.ok(approvedOrganizer);
    }

    @Operation(summary = "Lấy thông tin nhà tổ chức sự kiện theo SLUG")
    @GetMapping("/{slug}") 
    public ResponseEntity<OrganizersResponseDTO> getOrganizerBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(organizersService.getOrganizerBySlug(slug)); 
    }

    @Operation(summary = "Chỉnh sửa thông tin nhà tổ chức sự kiện")
    @PutMapping("/{slug}")
    @PreAuthorize("hasAuthority('SADMIN') or hasAuthority('ORGANIZER')") 
    public ResponseEntity<OrganizersResponseDTO> updateOrganizer(@PathVariable String slug, @Valid @RequestBody OrganizersRequestDTO requestDTO) {
        return ResponseEntity.ok(organizersService.updateOrganizer(slug, requestDTO));
    }

    @Operation(summary = "Xóa nhà tổ chức sự kiện")
    @DeleteMapping("/{slug}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<Void> deleteOrganizer(@PathVariable String slug) {
        organizersService.deleteOrganizer(slug);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy danh sách toàn bộ nhà quản lý sự kiện (SADMIN)")
    @GetMapping
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<OrganizersResponseDTO>> getAllOrganizersAsDTO() {
        List<OrganizersResponseDTO> responseDTOs = organizersService.getAllOrganizersAsDTO();
        return ResponseEntity.ok(responseDTOs);
    }
}
