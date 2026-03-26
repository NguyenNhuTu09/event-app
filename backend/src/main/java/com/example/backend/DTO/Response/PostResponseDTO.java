package com.example.backend.DTO.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostResponseDTO {
    private Long id;
    private String languageCode;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String seoTitle;
    private String seoDescription;

    // ===== 2 FIELD MỚI =====
    private String focusKeyword;
    private List<String> tags;

    private Map<String, String> alternateSlugs;

    private String thumbnailUrl;
    private String authorName;
    private Long viewCount;
    private LocalDateTime createdAt;

    private boolean isFeatured;
    private Long categoryId;
    private String categorySlug;
    private String categoryName;
    private Map<String, String> categoryAlternateSlugs;
}