package com.example.backend.DTO.Request;
import lombok.Data;

@Data
public class PostTranslationRequestDTO {
    private String title;
    private String summary;
    private String content;
    private String seoTitle;
    private String seoDescription;
}