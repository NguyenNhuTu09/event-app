package com.example.backend.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenterResponseDTO {

    private Integer presenterId;
    private String fullName;
    private String title;
    private String company;
    private String bio;
    private String avatarUrl;

    // Có thể mở rộng sau này:
    // private int totalActivities; (Số lượng hoạt động tham gia)
}
