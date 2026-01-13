package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.PresenterRequestDTO;
import com.example.backend.DTO.Response.PresenterResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Activity;
import com.example.backend.Models.Entity.FavoritePresenter;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.Presenters;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.FavoritePresenterRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.PresentersRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.Interface.PresenterService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenterServiceImpl implements PresenterService {
    private final PresentersRepository presentersRepository;
    private final ActivityRepository activityRepository;
    private final OrganizersRepository organizersRepository;

    private final FavoritePresenterRepository favoritePresenterRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng (User not found)"));
    }


    private Organizers getCurrentOrganizer() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentIdentity;

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            currentIdentity = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            currentIdentity = principal.toString();
        }

        final String emailToSearch = currentIdentity;

        Organizers organizer = organizersRepository.findByUser_Email(emailToSearch)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký làm Organizer hoặc tài khoản không tồn tại."));
        
        if (!organizer.isApproved()) {
            throw new IllegalArgumentException("Tài khoản Organizer của bạn chưa được phê duyệt.");
        }
        return organizer;
    }

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
        Organizers currentOrganizer = getCurrentOrganizer();

        if (presentersRepository.existsByFullNameAndCompany(requestDTO.getFullName(), requestDTO.getCompany())) {
             throw new RuntimeException("Diễn giả này đã tồn tại trong hệ thống (Trùng tên và công ty)");
        }

        Presenters presenter = mapToEntity(requestDTO);

        presenter.setOrganizer(currentOrganizer);

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
        try {
            LocalDateTime start = LocalDateTime.parse(startTimeStr);
            LocalDateTime end = LocalDateTime.parse(endTimeStr);
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
                entity.getAvatarUrl(),
                entity.isFeatured()
        );
    }

    @Override
    public List<PresenterResponseDTO> getPresentersByOrganizerSlug(String organizerSlug) {
        List<Presenters> presenters = presentersRepository.findByOrganizer_Slug(organizerSlug);
        return presenters.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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


    @Override
    public void toggleFavoritePresenter(Integer presenterId) {
        User currentUser = getCurrentUser();
        Presenters presenter = presentersRepository.findById(presenterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy diễn giả với ID: " + presenterId));

        Optional<FavoritePresenter> existingFavorite = favoritePresenterRepository
                .findByUser_IdAndPresenter_PresenterId(currentUser.getId(), presenterId);

        if (existingFavorite.isPresent()) {
            favoritePresenterRepository.delete(existingFavorite.get());
        } else {
            FavoritePresenter favorite = FavoritePresenter.builder()
                    .user(currentUser)
                    .presenter(presenter)
                    .likedAt(java.time.LocalDateTime.now())
                    .build();
            favoritePresenterRepository.save(favorite);
        }
    }

    @Override
    public List<PresenterResponseDTO> getMyFavoritePresenters() {
        User currentUser = getCurrentUser();
        
        List<FavoritePresenter> favorites = favoritePresenterRepository.findByUser_IdOrderByLikedAtDesc(currentUser.getId());
        
        return favorites.stream()
                .map(fav -> mapToDTO(fav.getPresenter())) 
                .collect(Collectors.toList());
    }

    @Override
    public List<PresenterResponseDTO> getFeaturedPresenters() {
        List<Presenters> featured = presentersRepository.findByIsFeaturedTrue();
        
        return featured.stream()
                .limit(4) 
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateFeaturedPresenters(List<Integer> presenterIds) {
        if (presenterIds.size() > 4) {
            throw new IllegalArgumentException("Chỉ được phép chọn tối đa 4 diễn giả nổi bật.");
        }

        presentersRepository.resetAllFeaturedPresenters();

        if (!presenterIds.isEmpty()) {
            List<Presenters> selectedPresenters = presentersRepository.findAllById(presenterIds);
            
            if (selectedPresenters.size() != presenterIds.size()) {
                throw new RuntimeException("Một số ID diễn giả không tồn tại.");
            }

            selectedPresenters.forEach(p -> p.setFeatured(true));
            presentersRepository.saveAll(selectedPresenters);
        }
    }

    @Override
    public List<PresenterResponseDTO> getMyPresenters() {
        Organizers currentOrganizer = getCurrentOrganizer();

        List<Presenters> presenters = presentersRepository.findByOrganizer_OrganizerId(currentOrganizer.getOrganizerId());

        return presenters.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}
