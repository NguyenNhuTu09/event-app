package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.ActivityRequestDTO;
import com.example.backend.DTO.Response.ActivityResponseDTO;

public interface ActivityService {

    ActivityResponseDTO createActivity(ActivityRequestDTO requestDTO);

    ActivityResponseDTO updateActivity(Integer activityId, ActivityRequestDTO requestDTO);

    void deleteActivity(Integer activityId);

    ActivityResponseDTO getActivityById(Integer activityId);

    // --- QUERY NGHIỆP VỤ ---

    List<ActivityResponseDTO> getActivitiesByEventId(Long eventId);

    List<ActivityResponseDTO> getActivitiesByPresenterId(Integer presenterId);

    List<ActivityResponseDTO> searchActivitiesInEvent(Long eventId, String keyword);

    String getActivityQrCode(Integer activityId);

    List<ActivityResponseDTO> getRegisteredActivitiesByEvent(Long eventId);
}