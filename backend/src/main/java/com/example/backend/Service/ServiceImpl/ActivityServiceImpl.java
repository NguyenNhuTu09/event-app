package com.example.backend.Service.ServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.ActivityRequestDTO;
import com.example.backend.DTO.Response.ActivityCategoryResponseDTO;
import com.example.backend.DTO.Response.ActivityResponseDTO;
import com.example.backend.DTO.Response.PresenterResponseDTO;
import com.example.backend.Models.Entity.Activity;
import com.example.backend.Models.Entity.ActivityCategories;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.Presenters;
import com.example.backend.Repository.ActivityCategoriesRepository;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.PresentersRepository;
import com.example.backend.Service.Interface.ActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final EventRepository eventRepository;
    private final ActivityCategoriesRepository categoryRepository;
    private final PresentersRepository presentersRepository;
    
    // Jackson Mapper để xử lý JSON
    private final ObjectMapper objectMapper; 

    @Override
    @Transactional
    public ActivityResponseDTO createActivity(ActivityRequestDTO requestDTO) {
        // 1. Validate Event
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện với ID: " + requestDTO.getEventId()));

        // 2. Validate Category
        ActivityCategories category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại hoạt động"));

        // 3. Validate Presenter (Nếu có)
        Presenters presenter = null;
        if (requestDTO.getPresenterId() != null) {
            presenter = presentersRepository.findById(requestDTO.getPresenterId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn giả"));
            
            // Check trùng lịch diễn giả
            // activityId = -1 để biểu thị đang tạo mới
            boolean isPresenterBusy = activityRepository.existsByPresenterConflict(
                    presenter.getPresenterId(), 
                    requestDTO.getStartTime(), 
                    requestDTO.getEndTime(), 
                    -1
            );
            
            if (isPresenterBusy) {
                throw new RuntimeException("Diễn giả " + presenter.getFullName() + " đã có lịch khác trong khung giờ này.");
            }
        }

        // 4. Check trùng phòng (Room Conflict)
        if (requestDTO.getRoomOrVenue() != null) {
            boolean isRoomBusy = activityRepository.existsByRoomConflict(
                    requestDTO.getEventId(),
                    requestDTO.getRoomOrVenue(),
                    requestDTO.getStartTime(),
                    requestDTO.getEndTime(),
                    -1
            );
            if (isRoomBusy) {
                throw new RuntimeException("Phòng " + requestDTO.getRoomOrVenue() + " đã có hoạt động khác trong khung giờ này.");
            }
        }

        // 5. Mapping & Save
        Activity activity = new Activity();
        mapRequestToEntity(requestDTO, activity, event, category, presenter);
        
        Activity savedActivity = activityRepository.save(activity);
        return mapToDTO(savedActivity);
    }

    @Override
    @Transactional
    public ActivityResponseDTO updateActivity(Integer activityId, ActivityRequestDTO requestDTO) {
        Activity existingActivity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoạt động để cập nhật"));

        // 1. Cập nhật Category nếu thay đổi
        if (!existingActivity.getCategory().getCategoryId().equals(requestDTO.getCategoryId())) {
            ActivityCategories newCategory = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Loại hoạt động không tồn tại"));
            existingActivity.setCategory(newCategory);
        }

        // 2. Cập nhật Presenter & Check Conflict
        if (requestDTO.getPresenterId() != null) {
            // Nếu diễn giả thay đổi HOẶC thời gian thay đổi -> Check lại lịch
            boolean isPresenterChanged = existingActivity.getPresenter() == null || 
                                         !existingActivity.getPresenter().getPresenterId().equals(requestDTO.getPresenterId());
            
            if (isPresenterChanged || isTimeChanged(existingActivity, requestDTO)) {
                 if (activityRepository.existsByPresenterConflict(
                         requestDTO.getPresenterId(), 
                         requestDTO.getStartTime(), 
                         requestDTO.getEndTime(), 
                         activityId)) { // Truyền ID hiện tại để loại trừ chính nó
                     throw new RuntimeException("Diễn giả bị trùng lịch trong khung giờ mới.");
                 }
                 Presenters newPresenter = presentersRepository.findById(requestDTO.getPresenterId())
                         .orElseThrow(() -> new RuntimeException("Diễn giả không tồn tại"));
                 existingActivity.setPresenter(newPresenter);
            }
        } else {
            existingActivity.setPresenter(null);
        }

        // 3. Check Room Conflict nếu đổi phòng hoặc đổi giờ
        if (isTimeChanged(existingActivity, requestDTO) || 
            (requestDTO.getRoomOrVenue() != null && !requestDTO.getRoomOrVenue().equals(existingActivity.getRoomOrVenue()))) {
            
            if (activityRepository.existsByRoomConflict(
                    existingActivity.getEvent().getEventId(),
                    requestDTO.getRoomOrVenue(),
                    requestDTO.getStartTime(),
                    requestDTO.getEndTime(),
                    activityId)) {
                throw new RuntimeException("Phòng " + requestDTO.getRoomOrVenue() + " bị trùng lịch.");
            }
        }

        // 4. Update fields cơ bản
        mapRequestToEntity(requestDTO, existingActivity, existingActivity.getEvent(), existingActivity.getCategory(), existingActivity.getPresenter());
        
        return mapToDTO(activityRepository.save(existingActivity));
    }

    @Override
    public void deleteActivity(Integer activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new RuntimeException("Không tìm thấy hoạt động");
        }
        activityRepository.deleteById(activityId);
    }

    @Override
    public ActivityResponseDTO getActivityById(Integer activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoạt động"));
        return mapToDTO(activity);
    }

    @Override
    public List<ActivityResponseDTO> getActivitiesByEventId(Long eventId) {
        List<Activity> activities = activityRepository.findByEvent_EventIdOrderByStartTimeAsc(eventId);
        return activities.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ActivityResponseDTO> getActivitiesByPresenterId(Integer presenterId) {
        List<Activity> activities = activityRepository.findByPresenter_PresenterIdOrderByStartTimeAsc(presenterId);
        return activities.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ActivityResponseDTO> searchActivitiesInEvent(Long eventId, String keyword) {
        List<Activity> activities = activityRepository.searchActivitiesInEvent(eventId, keyword);
        return activities.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // --- PRIVATE HELPER METHODS ---

    private boolean isTimeChanged(Activity old, ActivityRequestDTO distinct) {
        return !old.getStartTime().isEqual(distinct.getStartTime()) || 
               !old.getEndTime().isEqual(distinct.getEndTime());
    }

    // Helper: Map từ DTO -> Entity
    private void mapRequestToEntity(ActivityRequestDTO dto, Activity entity, 
                                    Event event, ActivityCategories category, Presenters presenter) {
        entity.setEvent(event);
        entity.setCategory(category);
        entity.setPresenter(presenter);
        
        entity.setActivityName(dto.getActivityName());
        entity.setDescription(dto.getDescription());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setMaxAttendees(dto.getMaxAttendees());
        entity.setRoomOrVenue(dto.getRoomOrVenue());
        entity.setMaterialsUrl(dto.getMaterialsUrl());

        // Convert List<String> -> JSON String
        try {
            if (dto.getAccessibleTo() != null) {
                String jsonStr = objectMapper.writeValueAsString(dto.getAccessibleTo());
                entity.setAccessibleTo(jsonStr);
            } else {
                entity.setAccessibleTo("[]");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý dữ liệu accessibleTo: " + e.getMessage());
        }
    }

    // Helper: Map từ Entity -> DTO
    private ActivityResponseDTO mapToDTO(Activity entity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        
        dto.setActivityId(entity.getActivityId());
        dto.setActivityName(entity.getActivityName());
        dto.setDescription(entity.getDescription());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setMaxAttendees(entity.getMaxAttendees());
        dto.setRoomOrVenue(entity.getRoomOrVenue());
        dto.setMaterialsUrl(entity.getMaterialsUrl());
        
        dto.setEventId(entity.getEvent().getEventId());

        // Map Category
        if (entity.getCategory() != null) {
            dto.setCategory(new ActivityCategoryResponseDTO(
                    entity.getCategory().getCategoryId(),
                    entity.getCategory().getCategoryName(),
                    entity.getCategory().getDescription()
            ));
        }

        // Map Presenter
        if (entity.getPresenter() != null) {
            Presenters p = entity.getPresenter();
            dto.setPresenter(new PresenterResponseDTO(
                    p.getPresenterId(),
                    p.getFullName(),
                    p.getTitle(),
                    p.getCompany(),
                    p.getBio(),
                    p.getAvatarUrl()
            ));
        }

        // Convert JSON String -> List<String>
        try {
            if (entity.getAccessibleTo() != null && !entity.getAccessibleTo().isEmpty()) {
                List<String> roles = objectMapper.readValue(
                        entity.getAccessibleTo(), 
                        new TypeReference<List<String>>(){}
                );
                dto.setAccessibleTo(roles);
            } else {
                dto.setAccessibleTo(new ArrayList<>());
            }
        } catch (Exception e) {
            // Nếu lỗi parse JSON (do data cũ lỗi), trả về list rỗng tránh crash app
            dto.setAccessibleTo(new ArrayList<>());
        }

        return dto;
    }
}
