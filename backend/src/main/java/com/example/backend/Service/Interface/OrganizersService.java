package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.Request.OrganizersRequestDTO;
import com.example.backend.DTO.Response.OrganizersResponseDTO;


public interface OrganizersService {
    List<OrganizersResponseDTO> getAllOrganizersAsDTO();
    OrganizersResponseDTO createOrganizer(OrganizersRequestDTO requestDTO);
    OrganizersResponseDTO getOrganizerById(Integer organizerId);
    OrganizersResponseDTO updateOrganizer(Integer organizerId, OrganizersRequestDTO requestDTO);
    void deleteOrganizer(Integer organizerId);
    OrganizersResponseDTO approveOrganizer(Integer organizerId);
}
