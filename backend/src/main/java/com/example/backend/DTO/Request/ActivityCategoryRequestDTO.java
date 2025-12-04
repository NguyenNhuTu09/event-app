package com.example.backend.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCategoryRequestDTO {

    @NotBlank(message = "Tên loại hoạt động không được để trống")
    @Size(max = 100, message = "Tên loại hoạt động tối đa 100 ký tự")
    private String categoryName;

    private String description;
}