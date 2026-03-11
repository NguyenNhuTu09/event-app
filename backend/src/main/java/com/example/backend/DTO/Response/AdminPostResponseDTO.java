package com.example.backend.DTO.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.backend.Utils.PostStatus;

import lombok.Builder;
import lombok.Data;

/**
 * DTO dành riêng cho Admin edit page.
 * Trả về toàn bộ translations trong 1 lần gọi,
 * giúp frontend không cần gọi 2 lần (vi + en).
 */
@Data
@Builder
public class AdminPostResponseDTO {
    private Long id;
    private String thumbnailUrl;
    private PostStatus status;
    private String authorName;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Key: langCode ("vi", "en"), Value: nội dung bản dịch đó
    private Map<String, TranslationDetail> translations;

    @Data
    @Builder
    public static class TranslationDetail {
        private String title;
        private String slug;
        private String summary;
        private String content;
        private String seoTitle;
        private String seoDescription;
        private String focusKeyword;  // FIELD MỚI
        private List<String> tags;    // FIELD MỚI
    }
}