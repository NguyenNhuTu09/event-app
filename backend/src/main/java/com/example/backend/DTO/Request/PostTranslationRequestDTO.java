package com.example.backend.DTO.Request;

import java.util.List;

import lombok.Data;

@Data
public class PostTranslationRequestDTO {
    private String title;
    private String summary;
    private String content;
    private String seoTitle;
    private String seoDescription;

    // ===== 2 FIELD MỚI =====
    private String focusKeyword;
    private List<String> tags;
}