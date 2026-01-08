package com.example.backend.Controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Response.PostResponseDTO;
import com.example.backend.Service.ServiceImpl.PostServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post Management")
public class PostController {

    private final PostServiceImpl postService;

    @Operation(summary = "Lấy danh sách tin tức/bài viết (có phân trang)")
    @SecurityRequirements()
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(postService.getAllPublishedPosts(
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        ));
    }

    @Operation(summary = "Lấy chi tiết tin tức/bài viết")
    @SecurityRequirements()
    @GetMapping("/{slug}")
    public ResponseEntity<PostResponseDTO> getPostDetail(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }
}
