package com.example.backend.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCategoryResponseDTO {
    private Integer categoryId;
    private String categoryName;
    private String description;
}