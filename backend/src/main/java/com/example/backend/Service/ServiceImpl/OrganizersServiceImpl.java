package com.example.backend.Service.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.OrganizersRequestDTO;
import com.example.backend.DTO.Response.OrganizersResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.Interface.OrganizersService;
import com.example.backend.Utils.Role;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizersServiceImpl implements OrganizersService {

    private final OrganizersRepository organizersRepository;
    private final UserRepository userRepository;

    @Override
    public List<OrganizersResponseDTO> getAllOrganizersAsDTO() {
        return organizersRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override 
    @Transactional
    public OrganizersResponseDTO createOrganizer(OrganizersRequestDTO requestDTO){
        String currentUsername = "";
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }
        User user = userRepository.findByUsername(currentUsername)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại."));

        if (organizersRepository.existsByUser_Username(currentUsername)) {
             throw new IllegalArgumentException("Bạn đã đăng ký làm Nhà tổ chức rồi. Vui lòng chờ phê duyệt hoặc cập nhật thông tin.");
        }

        if (organizersRepository.existsByName(requestDTO.getName())) {
            throw new IllegalArgumentException("Tên nhà tổ chức đã tồn tại: " + requestDTO.getName());
        }

        if (requestDTO.getContactEmail() != null && organizersRepository.existsByContactEmail(requestDTO.getContactEmail())) {
            throw new IllegalArgumentException("Email nhà tổ chức đã tồn tại: " + requestDTO.getContactEmail());
        }

        Organizers newOrganizer = new Organizers();
        newOrganizer.setName(requestDTO.getName());
        newOrganizer.setDescription(requestDTO.getDescription());
        newOrganizer.setLogoUrl(requestDTO.getLogoUrl());
        newOrganizer.setContactPhoneNumber(requestDTO.getContactPhoneNumber());
        newOrganizer.setContactEmail(requestDTO.getContactEmail());
        newOrganizer.setUser(user); 
        newOrganizer.setApproved(false); 
        Organizers savedOrganizer = organizersRepository.save(newOrganizer);
        return convertToResponseDTO(savedOrganizer);
    }

    @Override
    @Transactional
    public OrganizersResponseDTO approveOrganizer(Integer organizerId) {
        Organizers organizer = organizersRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));
        if (organizer.isApproved()) {
            throw new IllegalArgumentException("Organizer này đã được duyệt trước đó.");
        }
        organizer.setApproved(true);
        User owner = organizer.getUser();
        if (owner != null) {
            owner.setRole(Role.ORGANIZER); 
            userRepository.save(owner);
        }

        Organizers updatedOrganizer = organizersRepository.save(organizer);
        return convertToResponseDTO(updatedOrganizer);
    }


    @Override
    public OrganizersResponseDTO getOrganizerById(Integer organizerId) {
        Organizers organizer = organizersRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));
        return convertToResponseDTO(organizer);
    }

    @Override
    @Transactional
    public OrganizersResponseDTO updateOrganizer(Integer organizerId, OrganizersRequestDTO requestDTO) {
        Organizers existingOrganizer = organizersRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));

        organizersRepository.findByName(requestDTO.getName()).ifPresent(organizer -> {
            if (!organizer.getOrganizerId().equals(organizerId)) {
                throw new IllegalArgumentException("Organizer name already exists: " + requestDTO.getName());
            }
        });
        
        if (requestDTO.getContactEmail() != null) {
            organizersRepository.findByContactEmail(requestDTO.getContactEmail()).ifPresent(organizer -> {
                if (!organizer.getOrganizerId().equals(organizerId)) {
                    throw new IllegalArgumentException("Organizer email already exists: " + requestDTO.getContactEmail());
                }
            });
        }

        existingOrganizer.setName(requestDTO.getName());
        existingOrganizer.setDescription(requestDTO.getDescription());
        existingOrganizer.setLogoUrl(requestDTO.getLogoUrl());
        existingOrganizer.setContactPhoneNumber(requestDTO.getContactPhoneNumber());
        existingOrganizer.setContactEmail(requestDTO.getContactEmail());

        Organizers updatedOrganizer = organizersRepository.save(existingOrganizer);

        return convertToResponseDTO(updatedOrganizer);
    }

    @Override
    public void deleteOrganizer(Integer organizerId){
        if (!organizersRepository.existsById(organizerId)) {
            throw new ResourceNotFoundException("Organizer not found with id: " + organizerId);
        }
        organizersRepository.deleteById(organizerId);
    }

    private OrganizersResponseDTO convertToResponseDTO(Organizers organizer) {
        return OrganizersResponseDTO.builder()
            .organizerId(organizer.getOrganizerId())
            .name(organizer.getName())
            .description(organizer.getDescription())
            .logoUrl(organizer.getLogoUrl())
            .contactPhoneNumber(organizer.getContactPhoneNumber())
            .contactEmail(organizer.getContactEmail())
            .userId(organizer.getUser() != null ? organizer.getUser().getId() : null) 
            .username(organizer.getUser() != null ? organizer.getUser().getUsername() : null)
            .build();
    }
}
