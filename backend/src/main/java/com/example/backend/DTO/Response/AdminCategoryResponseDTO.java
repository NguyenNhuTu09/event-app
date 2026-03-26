package com.example.backend.DTO.Response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminCategoryResponseDTO {
    private Long id;
    private String slug;
    private Integer displayOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin parent
    private ParentInfo parent;

    // Toàn bộ translations: key = langCode ("vi", "en")
    private Map<String, TranslationDetail> translations;

    // Children (chỉ trả shallow — id, slug, translations)
    private List<AdminCategoryResponseDTO> children;

    @Data
    @Builder
    public static class ParentInfo {
        private Long id;
    }

    @Data
    @Builder
    public static class TranslationDetail {
        private String name;
        private String slug; 
        private String seoTitle;
        private String seoDescription;
    }
}