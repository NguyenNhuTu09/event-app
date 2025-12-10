package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.OrganizersRequestDTO;
import com.example.backend.DTO.Response.OrganizersResponseDTO;


public interface OrganizersService {
    List<OrganizersResponseDTO> getAllOrganizersAsDTO();
    OrganizersResponseDTO createOrganizer(OrganizersRequestDTO requestDTO);
    OrganizersResponseDTO getOrganizerBySlug(String slug);
    OrganizersResponseDTO updateOrganizer(String slug, OrganizersRequestDTO requestDTO);
    // void deleteOrganizer(Integer organizerId);
    void deleteOrganizer(String slug);
    OrganizersResponseDTO approveOrganizer(Integer organizerId);
}
