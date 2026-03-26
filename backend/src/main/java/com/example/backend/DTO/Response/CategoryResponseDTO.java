package com.example.backend.DTO.Response;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDTO {
    private Long id;
    private String slug;
    private String languageCode;
    private String name;
    private String seoTitle;
    private String seoDescription;
    private Integer displayOrder;
    private Boolean isActive;
    private Map<String, String> alternateSlugs;
    private List<CategoryResponseDTO> children;

    private ParentInfo parent;

    @Data
    @Builder
    public static class ParentInfo {
        private Long id;
        private String slug;
        private Map<String, String> alternateSlugs;
        private String name;
    }
}