package com.example.backend.Controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.DTO.Request.PostRequestDTO;
import com.example.backend.DTO.Response.PostResponseDTO;
import com.example.backend.Service.CloudinaryService;
import com.example.backend.Service.ServiceImpl.PostServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@Tag(name = "Admin Post Management")
public class AdminPostController {

    private final PostServiceImpl postService;
    private final CloudinaryService cloudinaryService; 

    @Operation(summary = "Tạo mới một tin tức/bài viết")
    @PostMapping
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<PostResponseDTO> createPost(@RequestBody PostRequestDTO request) {
        // Lấy userId từ SecurityContext (Token)
        Long userId = 1L; // Thay bằng hàm getCurrentUserId() của bạn
        return ResponseEntity.ok(postService.createPost(request, userId));
    }

    @Operation(summary = "Cập nhật một tin tức/bài viết")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long id, 
            @RequestBody PostRequestDTO request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @Operation(summary = "Xóa một tin tức/bài viết")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Tải lên hình ảnh cho trình soạn thảo")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImageForEditor(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", 0)); 
            }

            String imageUrl = cloudinaryService.uploadImage(file);

            return ResponseEntity.ok(Map.of(
                "success", 1,
                "file", Map.of("url", imageUrl)
            ));

        } catch (IOException e) {
            return ResponseEntity.ok(Map.of("success", 0));
        }
    }
}
