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
    private List<String> accessibleTo; 
    private String roomOrVenue;
    private String materialsUrl;
    private String activityImageUrl;


    private Long eventId;
    private ActivityCategoryResponseDTO category;
    private PresenterResponseDTO presenter; 
    private boolean isRegistered;
}