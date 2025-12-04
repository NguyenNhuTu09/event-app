package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.ActivityRequestDTO;
import com.example.backend.DTO.Response.ActivityResponseDTO;

public interface ActivityService {

    // --- CRUD ---
    
    // Tạo hoạt động mới (Có validate trùng lịch phòng/diễn giả)
    ActivityResponseDTO createActivity(ActivityRequestDTO requestDTO);

    // Cập nhật hoạt động
    ActivityResponseDTO updateActivity(Integer activityId, ActivityRequestDTO requestDTO);

    // Xóa hoạt động
    void deleteActivity(Integer activityId);

    // Lấy chi tiết 1 hoạt động
    ActivityResponseDTO getActivityById(Integer activityId);

    // --- QUERY NGHIỆP VỤ ---

    // 1. Lấy toàn bộ lịch trình của 1 sự kiện (Sắp xếp theo giờ) -> Dùng cho trang chi tiết sự kiện
    List<ActivityResponseDTO> getActivitiesByEventId(Long eventId);

    // 2. Lấy danh sách hoạt động của 1 diễn giả -> Dùng cho trang profile diễn giả
    List<ActivityResponseDTO> getActivitiesByPresenterId(Integer presenterId);

    // 3. Tìm kiếm hoạt động trong sự kiện (Search bar trong trang Agenda)
    List<ActivityResponseDTO> searchActivitiesInEvent(Long eventId, String keyword);
}