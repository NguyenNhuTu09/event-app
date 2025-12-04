package com.example.backend.Service.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.ActivityCategoryRequestDTO;
import com.example.backend.DTO.Response.ActivityCategoryResponseDTO;
import com.example.backend.Models.Entity.ActivityCategories;
import com.example.backend.Repository.ActivityCategoriesRepository;
import com.example.backend.Service.Interface.ActivityCategoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityCategoryServiceImpl implements ActivityCategoryService {
    private final ActivityCategoriesRepository categoryRepository;

    @Override
    public List<ActivityCategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityCategoryResponseDTO getCategoryById(Integer id) {
        ActivityCategories category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại hoạt động với ID: " + id));
        return mapToDTO(category);
    }

    @Override
    public ActivityCategoryResponseDTO createCategory(ActivityCategoryRequestDTO requestDTO) {
        if (categoryRepository.existsByCategoryName(requestDTO.getCategoryName())) {
            throw new RuntimeException("Tên loại hoạt động đã tồn tại");
        }
        ActivityCategories category = new ActivityCategories();
        category.setCategoryName(requestDTO.getCategoryName());
        category.setDescription(requestDTO.getDescription());

        return mapToDTO(categoryRepository.save(category));
    }

    @Override
    public ActivityCategoryResponseDTO updateCategory(Integer id, ActivityCategoryRequestDTO requestDTO) {
        ActivityCategories category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại hoạt động để cập nhật"));

        // Kiểm tra trùng tên nếu tên thay đổi
        if (!category.getCategoryName().equals(requestDTO.getCategoryName()) 
            && categoryRepository.existsByCategoryName(requestDTO.getCategoryName())) {
            throw new RuntimeException("Tên loại hoạt động mới đã tồn tại");
        }

        category.setCategoryName(requestDTO.getCategoryName());
        category.setDescription(requestDTO.getDescription());

        return mapToDTO(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy loại hoạt động để xóa");
        }
        categoryRepository.deleteById(id);
    }

    // Helper mapper
    private ActivityCategoryResponseDTO mapToDTO(ActivityCategories entity) {
        return new ActivityCategoryResponseDTO(
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getDescription()
        );
    }
}
