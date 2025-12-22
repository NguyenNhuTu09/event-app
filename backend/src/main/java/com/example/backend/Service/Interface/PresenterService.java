package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.PresenterRequestDTO;
import com.example.backend.DTO.Response.PresenterResponseDTO;

public interface PresenterService {
    List<PresenterResponseDTO> getAllPresenters();

    PresenterResponseDTO getPresenterById(Integer presenterId);

    PresenterResponseDTO createPresenter(PresenterRequestDTO requestDTO);

    PresenterResponseDTO updatePresenter(Integer presenterId, PresenterRequestDTO requestDTO);

    void deletePresenter(Integer presenterId);

    // --- CHỨC NĂNG NÂNG CAO ---

    List<PresenterResponseDTO> searchPresenters(String keyword);

    List<PresenterResponseDTO> getPresentersByEventId(Long eventId);

    boolean isPresenterBusy(Integer presenterId, String startTime, String endTime);

    List<PresenterResponseDTO> getPresentersByOrganizerSlug(String organizerSlug);

    void toggleFavoritePresenter(Integer presenterId);

    List<PresenterResponseDTO> getMyFavoritePresenters();

    List<PresenterResponseDTO> getFeaturedPresenters();
    
    void updateFeaturedPresenters(List<Integer> presenterIds);
}
