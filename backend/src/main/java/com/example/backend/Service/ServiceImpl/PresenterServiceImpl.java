package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.PresenterRequestDTO;
import com.example.backend.DTO.Response.PresenterResponseDTO;
import com.example.backend.Models.Entity.Activity;
import com.example.backend.Models.Entity.Presenters;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.PresentersRepository;
import com.example.backend.Service.Interface.PresenterService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenterServiceImpl implements PresenterService {
    private final PresentersRepository presentersRepository;
    private final ActivityRepository activityRepository;

    @Override
    public List<PresenterResponseDTO> getAllPresenters() {
        return presentersRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PresenterResponseDTO getPresenterById(Integer presenterId) {
        Presenters presenter = presentersRepository.findById(presenterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn giả với ID: " + presenterId));
        return mapToDTO(presenter);
    }

    @Override
    public PresenterResponseDTO createPresenter(PresenterRequestDTO requestDTO) {
        if (presentersRepository.existsByFullNameAndCompany(requestDTO.getFullName(), requestDTO.getCompany())) {
            throw new RuntimeException("Diễn giả này đã tồn tại trong hệ thống (Trùng tên và công ty)");
        }
        Presenters presenter = mapToEntity(requestDTO);
        Presenters savedPresenter = presentersRepository.save(presenter);
        return mapToDTO(savedPresenter);
    }

    @Override
    public PresenterResponseDTO updatePresenter(Integer presenterId, PresenterRequestDTO requestDTO) {
        Presenters existingPresenter = presentersRepository.findById(presenterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn giả để cập nhật"));

        existingPresenter.setFullName(requestDTO.getFullName());
        existingPresenter.setTitle(requestDTO.getTitle());
        existingPresenter.setCompany(requestDTO.getCompany());
        existingPresenter.setBio(requestDTO.getBio());
        existingPresenter.setAvatarUrl(requestDTO.getAvatarUrl());

        Presenters updatedPresenter = presentersRepository.save(existingPresenter);
        return mapToDTO(updatedPresenter);
    }

    @Override
    public void deletePresenter(Integer presenterId) {
        if (!presentersRepository.existsById(presenterId)) {
            throw new RuntimeException("Không tìm thấy diễn giả để xóa");
        }
        presentersRepository.deleteById(presenterId);
    }

    // --- CHỨC NĂNG NÂNG CAO ---

    @Override
    public List<PresenterResponseDTO> searchPresenters(String keyword) {
        return presentersRepository.searchByKeyword(keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PresenterResponseDTO> getPresentersByEventId(Long eventId) {
        // Logic:
        // 1. Lấy tất cả Activity của Event đó
        // 2. Lấy ra Presenter từ Activity
        // 3. Lọc bỏ null (Activity không có diễn giả)
        // 4. Lọc bỏ trùng lặp (1 diễn giả có thể nói 2 bài)
        
        List<Activity> activities = activityRepository.findByEvent_EventIdOrderByStartTimeAsc(eventId);

        return activities.stream()
                .map(Activity::getPresenter)           
                .filter(Objects::nonNull)              
                .distinct()                            
                .map(this::mapToDTO)                   
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPresenterBusy(Integer presenterId, String startTimeStr, String endTimeStr) {
        // Parse String sang LocalDateTime
        // Giả định client gửi format chuẩn ISO hoặc format cụ thể "yyyy-MM-dd HH:mm:ss"
        // Ở đây tôi dùng ISO_LOCAL_DATE_TIME (vd: "2023-12-03T10:00:00")
        try {
            LocalDateTime start = LocalDateTime.parse(startTimeStr);
            LocalDateTime end = LocalDateTime.parse(endTimeStr);

            // Gọi Repository kiểm tra conflict
            // currentActivityId = -1 vì đây là check chung, ko phải check khi update activity cụ thể
            return activityRepository.existsByPresenterConflict(presenterId, start, end, -1);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi định dạng ngày tháng: " + e.getMessage());
        }
    }

    private PresenterResponseDTO mapToDTO(Presenters entity) {
        return new PresenterResponseDTO(
                entity.getPresenterId(),
                entity.getFullName(),
                entity.getTitle(),
                entity.getCompany(),
                entity.getBio(),
                entity.getAvatarUrl()
        );
    }

    private Presenters mapToEntity(PresenterRequestDTO dto) {
        Presenters entity = new Presenters();
        entity.setFullName(dto.getFullName());
        entity.setTitle(dto.getTitle());
        entity.setCompany(dto.getCompany());
        entity.setBio(dto.getBio());
        entity.setAvatarUrl(dto.getAvatarUrl());
        return entity;
    }
}
