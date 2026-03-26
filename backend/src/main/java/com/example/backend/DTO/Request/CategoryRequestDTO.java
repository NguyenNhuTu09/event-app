package com.example.backend.DTO.Request;

import java.util.Map;

import lombok.Data;

@Data
public class CategoryRequestDTO {
    private Integer displayOrder;
    private Boolean isActive;
    private Long parentId;              // null = category gốc

    private Map<String, CategoryTranslationRequestDTO> translations;
}