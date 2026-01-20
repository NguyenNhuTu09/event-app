package com.example.backend.Service.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.OrganizerUnlockRequestDTO;
import com.example.backend.DTO.Request.OrganizersRequestDTO;
import com.example.backend.DTO.Response.OrganizerStatusResponseDTO;
import com.example.backend.DTO.Response.OrganizersResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.EmailService;
import com.example.backend.Service.Interface.OrganizersService;
import com.example.backend.Utils.Role;
import com.github.slugify.Slugify;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizersServiceImpl implements OrganizersService {

    private final OrganizersRepository organizersRepository;
    private final UserRepository userRepository;
    private final Slugify slugify = Slugify.builder().build();
    private final EmailService emailService;

    private String generateUniqueSlug(String name) {
        String baseSlug = slugify.slugify(name);
        String finalSlug = baseSlug;
        int count = 1;
        
        while (organizersRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + count;
            count++;
        }
        return finalSlug;
    }

    @Override
    public List<OrganizersResponseDTO> getAllOrganizersAsDTO() {
        return organizersRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override 
    @Transactional
    public OrganizersResponseDTO createOrganizer(OrganizersRequestDTO requestDTO){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentIdentity; 
        if (principal instanceof UserDetails) {
            currentIdentity = ((UserDetails) principal).getUsername();
        } else {
            currentIdentity = principal.toString();
        }

        final String emailToSearch = currentIdentity; 

        User user = userRepository.findByEmail(emailToSearch) 
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email/username: " + emailToSearch));

        if (organizersRepository.existsByUser(user)) {
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
        newOrganizer.setSlug(generateUniqueSlug(requestDTO.getName()));
        newOrganizer.setDescription(requestDTO.getDescription());
        newOrganizer.setLogoUrl(requestDTO.getLogoUrl());
        newOrganizer.setContactPhoneNumber(requestDTO.getContactPhoneNumber());
        newOrganizer.setContactEmail(requestDTO.getContactEmail());
        newOrganizer.setUser(user); 
        newOrganizer.setApproved(false); 
        
        Organizers savedOrganizer = organizersRepository.save(newOrganizer);
        try {
            emailService.sendOrganizerRegistrationPending(
                user.getEmail(),
                user.getUsername(),
                savedOrganizer.getName()
            );
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail Organizer Pending: " + e.getMessage());
        }
        return convertToResponseDTO(savedOrganizer);
    }

    public OrganizersResponseDTO getOrganizerBySlug(String slug) {
        Organizers organizer = organizersRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with slug: " + slug));
        return convertToResponseDTO(organizer);
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
        try {
            emailService.sendOrganizerApproved(
                owner.getEmail(),
                owner.getUsername(),
                updatedOrganizer.getName()
            );
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail Organizer Approved: " + e.getMessage());
        }
        return convertToResponseDTO(updatedOrganizer);
    }

    @Override
    @Transactional
    public OrganizersResponseDTO updateOrganizer(String slug, OrganizersRequestDTO requestDTO) {
        Organizers existingOrganizer = organizersRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with slug: " + slug));

        Integer currentId = existingOrganizer.getOrganizerId();

        organizersRepository.findByName(requestDTO.getName()).ifPresent(organizer -> {
            if (!organizer.getOrganizerId().equals(currentId)) {
                throw new IllegalArgumentException("Organizer name already exists: " + requestDTO.getName());
            }
        });
        
        if (requestDTO.getContactEmail() != null) {
            organizersRepository.findByContactEmail(requestDTO.getContactEmail()).ifPresent(organizer -> {
                if (!organizer.getOrganizerId().equals(currentId)) {
                    throw new IllegalArgumentException("Organizer email already exists: " + requestDTO.getContactEmail());
                }
            });
        }

        if (!existingOrganizer.getName().equals(requestDTO.getName())) {
            existingOrganizer.setName(requestDTO.getName());
            existingOrganizer.setSlug(generateUniqueSlug(requestDTO.getName())); 
        }

        existingOrganizer.setDescription(requestDTO.getDescription());
        existingOrganizer.setLogoUrl(requestDTO.getLogoUrl());
        existingOrganizer.setContactPhoneNumber(requestDTO.getContactPhoneNumber());
        existingOrganizer.setContactEmail(requestDTO.getContactEmail());

        Organizers updatedOrganizer = organizersRepository.save(existingOrganizer);

        return convertToResponseDTO(updatedOrganizer);
    }

    @Override
    public void deleteOrganizer(String slug){
        Organizers organizer = organizersRepository.findBySlug(slug)
             .orElseThrow(() -> new ResourceNotFoundException("Not found"));
        organizersRepository.delete(organizer);
    }

    private OrganizersResponseDTO convertToResponseDTO(Organizers organizer) {
        return OrganizersResponseDTO.builder()
            .organizerId(organizer.getOrganizerId())
            .slug(organizer.getSlug())
            .name(organizer.getName())
            .description(organizer.getDescription())
            .logoUrl(organizer.getLogoUrl())
            .contactPhoneNumber(organizer.getContactPhoneNumber())
            .contactEmail(organizer.getContactEmail())
            .userId(organizer.getUser() != null ? organizer.getUser().getId() : null) 
            .username(organizer.getUser() != null ? organizer.getUser().getUsername() : null)
            .isApproved(organizer.isApproved())
            .isLocked(organizer.isLocked())
            .isUnlockRequested(organizer.isUnlockRequested())
            .unlockRequestReason(organizer.getUnlockRequestReason())
            .build();
    }

    @Override
    @Transactional
    public void rejectOrganizer(Integer organizerId, String reason) {
        Organizers organizer = organizersRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));

        if (organizer.isApproved()) {
            throw new IllegalArgumentException("Không thể từ chối Organizer đã được duyệt (Hãy dùng chức năng Khóa/Xóa).");
        }

        User owner = organizer.getUser();
        String organizerName = organizer.getName();

        organizersRepository.delete(organizer);

        try {
            String finalReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Thông tin đăng ký không hợp lệ.";
            
            emailService.sendOrganizerRejected(
                owner.getEmail(),
                owner.getUsername(),
                organizerName,
                finalReason
            );
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail Organizer Rejected: " + e.getMessage());
        }
    }

    // --- CHỨC NĂNG 1: SADMIN khóa tài khoản ---
    @Override
    @Transactional
    public void lockOrganizer(Integer organizerId) {
        Organizers organizer = organizersRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));

        if (organizer.isLocked()) {
            throw new IllegalArgumentException("Organizer này đã bị khóa rồi.");
        }

        organizer.setLocked(true);
        organizersRepository.save(organizer);

        try {
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail lock: " + e.getMessage());
        }
    }

    // --- CHỨC NĂNG 2: SADMIN mở khóa (Chấp nhận yêu cầu) ---
    @Override
    @Transactional
    public void unlockOrganizer(Integer organizerId) {
        Organizers organizer = organizersRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));

        if (!organizer.isLocked()) {
            throw new IllegalArgumentException("Organizer này đang hoạt động bình thường, không cần mở khóa.");
        }

        organizer.setLocked(false);
        organizer.setUnlockRequested(false); // Reset trạng thái yêu cầu
        organizer.setUnlockRequestReason(null);
        organizersRepository.save(organizer);

        // Optional: Gửi mail thông báo đã mở khóa
    }

    @Override
    public OrganizerStatusResponseDTO getMyOrganizerStatus() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentEmail;
        if (principal instanceof UserDetails) {
            currentEmail = ((UserDetails) principal).getUsername();
        } else {
            currentEmail = principal.toString();
        }

        Organizers organizer = organizersRepository.findByUser_Email(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa đăng ký làm Organizer."));

        return OrganizerStatusResponseDTO.builder()
                .organizerName(organizer.getName())
                .slug(organizer.getSlug())
                .isApproved(organizer.isApproved())
                .isLocked(organizer.isLocked())
                .isUnlockRequested(organizer.isUnlockRequested())
                .build();
    }

    @Override
    @Transactional
    public void requestUnlock(OrganizerUnlockRequestDTO requestDTO) { 
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentEmail;
        
        if (principal instanceof UserDetails) {
            currentEmail = ((UserDetails) principal).getUsername();
        } else {
            currentEmail = principal.toString();
        }
        Organizers organizer = organizersRepository.findByUser_Email(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin Organizer của bạn."));

        if (!organizer.isLocked()) {
            throw new IllegalArgumentException("Tài khoản của bạn hiện đang hoạt động bình thường, không cần mở khóa.");
        }
        
        if (organizer.isUnlockRequested()) {
            throw new IllegalArgumentException("Bạn đã gửi yêu cầu rồi. Vui lòng chờ SADMIN phê duyệt.");
        }

        organizer.setUnlockRequested(true);
        organizer.setUnlockRequestReason(requestDTO.getReason());
        organizersRepository.save(organizer);
    }
}
