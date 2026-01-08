package com.example.backend.DTO.Response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String thumbnailUrl;
    private String authorName;
    private Long viewCount;
    private LocalDateTime createdAt;
}
