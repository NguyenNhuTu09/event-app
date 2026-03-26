package com.example.backend.DTO.Request;

import lombok.Data;

@Data
public class CategoryTranslationRequestDTO {
    private String slug; 
    private String name;
    private String seoTitle;
    private String seoDescription;
}