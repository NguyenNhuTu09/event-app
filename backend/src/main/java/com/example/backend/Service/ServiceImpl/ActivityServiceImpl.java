package com.example.backend.Service.ServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.ActivityRequestDTO;
import com.example.backend.DTO.Response.ActivityCategoryResponseDTO;
import com.example.backend.DTO.Response.ActivityResponseDTO;
import com.example.backend.DTO.Response.PresenterResponseDTO;
import com.example.backend.Models.Entity.Activity;
import com.example.backend.Models.Entity.ActivityCategories;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.Presenters;
import com.example.backend.Repository.ActivityAttendeesRepository;
import com.example.backend.Repository.ActivityCategoriesRepository;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.PresentersRepository;
import com.example.backend.Service.Interface.ActivityService;
import com.example.backend.Utils.RegistrationStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {
    private final ActivityRepository activityRepository;
    private final EventRepository eventRepository;
    private final ActivityCategoriesRepository categoryRepository;
    private final PresentersRepository presentersRepository;
    private final OrganizersRepository organizersRepository; 
    private final ActivityAttendeesRepository activityAttendeesRepository;
    private final ObjectMapper objectMapper; 

    private Organizers getCurrentOrganizer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return organizersRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng ký làm Organizer"));
    }

    private String getCurrentUserEmailSafe() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            // Log xem principal là gì
            System.out.println("Principal class: " + principal.getClass().getName());
            System.out.println("Principal value: " + principal.toString());

            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else if (principal instanceof String && !"anonymousUser".equals(principal)) {
                 return (String) principal;
            } else if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User) {
                // Nếu dùng Google Login/OAuth2
                return ((org.springframework.security.oauth2.core.user.DefaultOAuth2User) principal).getAttribute("email");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    private List<ActivityResponseDTO> processActivityList(List<Activity> activities, Long eventId) {
        if (activities == null || activities.isEmpty()) {
            return new ArrayList<>();
        }

        String currentUserEmail = getCurrentUserEmailSafe();
        
        // --- LOG DEBUG BẮT ĐẦU ---
        System.out.println("DEBUG CHECK: EventId = " + eventId);
        System.out.println("DEBUG CHECK: User Email = " + currentUserEmail);
        // --- LOG DEBUG KẾT THÚC ---

        List<Integer> registeredIds = new ArrayList<>();

        if (currentUserEmail != null && eventId != null) {
            List<RegistrationStatus> validStatuses = Arrays.asList(
                    RegistrationStatus.PENDING, 
                    RegistrationStatus.APPROVED
            );
            
            registeredIds = activityAttendeesRepository.findRegisteredActivityIds(
                    currentUserEmail, 
                    eventId, 
                    validStatuses
            );
            
            // --- LOG DEBUG ---
            System.out.println("DEBUG CHECK: Registered IDs found in DB: " + registeredIds);
        } else {
             System.out.println("DEBUG CHECK: User not logged in or EventId null");
        }

        final List<Integer> finalRegisteredIds = registeredIds;

        return activities.stream().map(activity -> {
            ActivityResponseDTO dto = mapToDTO(activity);
            
            // Kiểm tra xem ID của activity hiện tại có nằm trong list đã đăng ký không
            if (finalRegisteredIds.contains(activity.getActivityId())) {
                dto.setRegistered(true); 
            } else {
                dto.setRegistered(false);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ActivityResponseDTO> getActivitiesByEventId(Long eventId) {
        List<Activity> activities = activityRepository.findByEvent_EventIdOrderByStartTimeAsc(eventId);
        return processActivityList(activities, eventId);
    }



    @Override
    @Transactional
    public ActivityResponseDTO createActivity(ActivityRequestDTO requestDTO) {
        Event event = eventRepository.findById(requestDTO.getEventId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sự kiện với ID: " + requestDTO.getEventId()));

        ActivityCategories category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại hoạt động"));

        Presenters presenter = null;
        if (requestDTO.getPresenterId() != null) {
            presenter = presentersRepository.findById(requestDTO.getPresenterId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn giả"));
            
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

        if (!existingActivity.getCategory().getCategoryId().equals(requestDTO.getCategoryId())) {
            ActivityCategories newCategory = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Loại hoạt động không tồn tại"));
            existingActivity.setCategory(newCategory);
        }

        if (requestDTO.getPresenterId() != null) {
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
    public List<ActivityResponseDTO> getActivitiesByPresenterId(Integer presenterId) {
        // Lưu ý: Hàm này trả về hoạt động từ NHIỀU event khác nhau.
        // Việc check registered ở đây phức tạp hơn (cần loop query hoặc query in).
        // Tạm thời trả về mapToDTO thường (isRegistered = false) hoặc
        // nếu cần thiết phải implement logic phức tạp hơn check từng eventId.
        
        List<Activity> activities = activityRepository.findByPresenter_PresenterIdOrderByStartTimeAsc(presenterId);
        return activities.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ActivityResponseDTO> searchActivitiesInEvent(Long eventId, String keyword) {
        List<Activity> activities = activityRepository.searchActivitiesInEvent(eventId, keyword);
        return processActivityList(activities, eventId);
    }

    // --- PRIVATE HELPER METHODS ---

    private boolean isTimeChanged(Activity old, ActivityRequestDTO distinct) {
        return !old.getStartTime().isEqual(distinct.getStartTime()) || 
               !old.getEndTime().isEqual(distinct.getEndTime());
    }

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
                    p.getAvatarUrl(),
                    p.isFeatured()
            ));
        }

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
            dto.setAccessibleTo(new ArrayList<>());
        }
        return dto;
    }

    @Override
    public String getActivityQrCode(Integer activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoạt động"));

        Organizers currentOrganizer = getCurrentOrganizer();
        if (!activity.getEvent().getOrganizer().getOrganizerId().equals(currentOrganizer.getOrganizerId())) {
            throw new RuntimeException("Bạn không có quyền xem mã QR của hoạt động này.");
        }
        return activity.getActivityQrCode();
    }

    @Override
    public List<ActivityResponseDTO> getRegisteredActivitiesByEvent(Long eventId) {
        String email = getCurrentUserEmailSafe();
        if (email == null) {
            throw new RuntimeException("Bạn cần đăng nhập để xem danh sách hoạt động đã đăng ký.");
        }

        List<RegistrationStatus> validStatuses = Arrays.asList(
                RegistrationStatus.PENDING, 
                RegistrationStatus.APPROVED
        );

        List<Activity> registeredActivities = activityRepository.findRegisteredActivitiesByEventAndUser(
                eventId, 
                email, 
                validStatuses
        );

        return registeredActivities.stream().map(activity -> {
            ActivityResponseDTO dto = mapToDTO(activity);
            dto.setRegistered(true); 
            return dto;
        }).collect(Collectors.toList());
    }
}
