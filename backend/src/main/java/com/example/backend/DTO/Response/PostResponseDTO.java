package com.example.backend.DTO.Response;

import java.time.LocalDateTime;
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

    private Map<String, String> alternateSlugs;
    
    private String thumbnailUrl;
    private String authorName;
    private Long viewCount;
    private LocalDateTime createdAt;
}