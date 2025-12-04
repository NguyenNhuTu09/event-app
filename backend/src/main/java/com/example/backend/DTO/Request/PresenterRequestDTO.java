package com.example.backend.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenterRequestDTO {

    @NotBlank(message = "Họ và tên diễn giả không được để trống")
    @Size(max = 255, message = "Họ và tên không được vượt quá 255 ký tự")
    private String fullName;

    @Size(max = 255, message = "Chức danh không được vượt quá 255 ký tự")
    private String title;

    @Size(max = 255, message = "Tên công ty không được vượt quá 255 ký tự")
    private String company;

    private String bio;

    private String avatarUrl;
}
