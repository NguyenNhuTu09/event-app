package com.example.backend.Controller;

import java.util.List;
import java.util.Map;

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

import com.example.backend.DTO.Request.CategoryRequestDTO;
import com.example.backend.DTO.Response.AdminCategoryResponseDTO;
import com.example.backend.Service.ServiceImpl.CategoryServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin Category Post Management")
public class AdminCategoryController {

    private final CategoryServiceImpl categoryService;

    @Operation(summary = "Lấy danh sách tất cả category (đủ translations)")
    @GetMapping
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<AdminCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllForAdmin());
    }

    @Operation(summary = "Lấy chi tiết category theo ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<AdminCategoryResponseDTO> getCategoryById(
            @PathVariable Long id) {

        return ResponseEntity.ok(categoryService.getCategoryByIdForAdmin(id));
    }

    @Operation(summary = "Tạo mới category")
    @PostMapping
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<AdminCategoryResponseDTO> createCategory(
            @RequestBody CategoryRequestDTO request) {

        return ResponseEntity.status(201).body(categoryService.createCategory(request));
    }

    @Operation(summary = "Cập nhật category theo ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<AdminCategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequestDTO request) {

        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @Operation(summary = "Xóa category theo ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        String message = categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", message
        ));
    }
}