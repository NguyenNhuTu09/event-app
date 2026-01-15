package com.example.backend.DTO.Response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponseDTO {

    private Integer activityId;
    
    // Thông tin cơ bản
    private String activityName;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long maxAttendees;
    private List<String> accessibleTo; // Đã convert từ JSON String trong DB ra List
    private String roomOrVenue;
    private String materialsUrl;

    // --- Thông tin liên kết (Flatten hoặc Nested Object) ---

    // 1. Thuộc Event nào
    private Long eventId;
    
    // 2. Loại hoạt động (Trả về object nhỏ hoặc tên)
    private ActivityCategoryResponseDTO category;

    // 3. Diễn giả (Trả về full DTO để hiển thị ảnh, chức danh...)
    private PresenterResponseDTO presenter; 
    private boolean isRegistered;
}