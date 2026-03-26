package com.example.backend.Controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.DTO.Request.FeaturedPostRequestDTO;
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
        Long userId = 1L; 
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

    @Operation(summary = "Lấy chi tiết bài viết theo ID (Dùng cho Admin Edit)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<PostResponseDTO> getPostById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "vi") String lang) { 
        return ResponseEntity.ok(postService.getPostById(id, lang));
    }

    // @Operation(summary = "Lấy chi tiết bài viết theo ID (Dùng cho Admin Edit)")
    // @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('SADMIN')")
    // public ResponseEntity<AdminPostResponseDTO> getPostById(@PathVariable Long id) {
    //     return ResponseEntity.ok(postService.getPostByIdForAdmin(id));
    // }

    @Operation(summary = "Xóa một tin tức/bài viết")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Tải lên hình ảnh/video cho trình soạn thảo")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImageForEditor(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", 0)); 
            }
            
            // SỬA DÒNG NÀY: Gọi hàm uploadMedia thay vì uploadImage
            String fileUrl = cloudinaryService.uploadMedia(file); 
            
            return ResponseEntity.ok(Map.of(
                "success", 1,
                "file", Map.of("url", fileUrl) // Trả về url như cũ cho Editor (CKEditor / EditorJS)
            ));
        } catch (IOException e) {
            return ResponseEntity.ok(Map.of("success", 0));
        }
    }

    @Operation(summary = "Lấy danh sách bài viết nổi bật hiện tại (Admin)")
    @GetMapping("/featured")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<PostResponseDTO>> getFeaturedPostsForAdmin(
            @RequestParam(defaultValue = "vi") String lang) {
        return ResponseEntity.ok(postService.getFeaturedPosts(lang));
    }

    @Operation(summary = "Cập nhật danh sách bài viết nổi bật (Admin)")
    @PutMapping("/featured")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<PostResponseDTO>> updateFeaturedPosts(
            @RequestBody FeaturedPostRequestDTO request) {
        return ResponseEntity.ok(postService.updateFeaturedPosts(request));
    }


    @Operation(summary = "Xóa một bài viết khỏi danh sách nổi bật (Admin)")
    @DeleteMapping("/featured/{postId}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<Map<String, Object>> removeFeaturedPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.removeFeaturedPost(postId));
    }
}