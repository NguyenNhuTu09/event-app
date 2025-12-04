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

import com.example.backend.DTO.Request.ActivityCategoryRequestDTO;
import com.example.backend.DTO.Response.ActivityCategoryResponseDTO;
import com.example.backend.Service.Interface.ActivityCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activity-categories")
@RequiredArgsConstructor
@Tag(name = "Activity Categories")
public class ActivityCategoryController {
    private final ActivityCategoryService categoryService;
    // --- PUBLIC ---

    @Operation(summary = "Lấy danh sách tất cả loại hoạt động")
    @GetMapping
    public ResponseEntity<List<ActivityCategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Operation(summary = "Lấy chi tiết loại hoạt động theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ActivityCategoryResponseDTO> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // --- ADMIN / ORGANIZER ONLY ---

    @Operation(summary = "Tạo loại hoạt động mới (SADMIN, ORGANIZER)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SADMIN', 'ORGANIZER')")
    public ResponseEntity<ActivityCategoryResponseDTO> createCategory(@Valid @RequestBody ActivityCategoryRequestDTO requestDTO) {
        return new ResponseEntity<>(categoryService.createCategory(requestDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Cập nhật loại hoạt động (SADMIN, ORGANIZER)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SADMIN', 'ORGANIZER')")
    public ResponseEntity<ActivityCategoryResponseDTO> updateCategory(@PathVariable Integer id, 
                                                                      @Valid @RequestBody ActivityCategoryRequestDTO requestDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, requestDTO));
    }

    @Operation(summary = "Xóa loại hoạt động (SADMIN, ORGANIZER)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SADMIN', 'ORGANIZER')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
