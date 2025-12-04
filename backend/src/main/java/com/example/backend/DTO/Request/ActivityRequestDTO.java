package com.example.backend.DTO.Request;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequestDTO {

    @NotNull(message = "Event ID là bắt buộc")
    private Long eventId; // Liên kết với Event (Long)

    @NotNull(message = "Category ID là bắt buộc")
    private Integer categoryId; // Liên kết với Category (Integer)

    private Integer presenterId; // Có thể null nếu hoạt động không có diễn giả

    @NotBlank(message = "Tên hoạt động không được để trống")
    @Size(max = 255)
    private String activityName;

    private String description;

    @NotNull(message = "Thời gian bắt đầu là bắt buộc")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc là bắt buộc")
    private LocalDateTime endTime;

    private Long maxAttendees;

    // Frontend gửi lên dạng mảng: ["ROLE_VIP", "ROLE_USER"]
    // Service sẽ convert sang String JSON để lưu DB
    private List<String> accessibleTo; 

    private String roomOrVenue;

    private String materialsUrl;
}