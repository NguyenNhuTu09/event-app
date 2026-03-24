package com.example.backend.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Response.CategoryResponseDTO;
import com.example.backend.Service.ServiceImpl.CategoryServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category Post Management")
public class CategoryController {

    private final CategoryServiceImpl categoryService;

    @Operation(summary = "Lấy toàn bộ cây category theo ngôn ngữ")
    @SecurityRequirements()
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategoryTree(
            @RequestParam(defaultValue = "vi") String lang) {

        return ResponseEntity.ok(categoryService.getCategoryTree(lang));
    }

    @Operation(summary = "Lấy chi tiết category theo slug")
    @SecurityRequirements()
    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponseDTO> getCategoryBySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "vi") String lang) {

        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug, lang));
    }
}