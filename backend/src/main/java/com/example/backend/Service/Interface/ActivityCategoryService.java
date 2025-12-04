package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.ActivityCategoryRequestDTO;
import com.example.backend.DTO.Response.ActivityCategoryResponseDTO;

public interface ActivityCategoryService {
    List<ActivityCategoryResponseDTO> getAllCategories();
    ActivityCategoryResponseDTO getCategoryById(Integer id);
    ActivityCategoryResponseDTO createCategory(ActivityCategoryRequestDTO requestDTO);
    ActivityCategoryResponseDTO updateCategory(Integer id, ActivityCategoryRequestDTO requestDTO);
    void deleteCategory(Integer id);
}
