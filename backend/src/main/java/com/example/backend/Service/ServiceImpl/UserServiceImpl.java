package com.example.backend.Service.ServiceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.ChangePasswordRequestDTO;
import com.example.backend.DTO.UserResponseDTO;
import com.example.backend.DTO.UserUpdateDTO;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + id));
        return convertToDto(user);
    }

    @Override
    public UserResponseDTO getCurrentUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
        return convertToDto(user);
    }

    @Override
    public UserResponseDTO updateCurrentUserProfile(UserUpdateDTO userUpdateDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User userToUpdate = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));
        
        userToUpdate.setAddress(userUpdateDTO.getAddress());
        userToUpdate.setGender(userUpdateDTO.getGender());
        userToUpdate.setDateOfBirth(userUpdateDTO.getDateOfBirth());
        userToUpdate.setPhoneNumber(userUpdateDTO.getPhoneNumber());

        User updatedUser = userRepository.save(userToUpdate);
        return convertToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với ID: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO convertToDto(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());
        dto.setGender(user.getGender());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setRole(user.getRole());
        return dto;
    }

    @Override
    public void changeCurrentUserPassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

        if (!passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
        }

        if (!changePasswordRequestDTO.getNewPassword().equals(changePasswordRequestDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không trùng khớp.");
        }

        String encodedNewPassword = passwordEncoder.encode(changePasswordRequestDTO.getNewPassword());

        currentUser.setPassword(encodedNewPassword);
        userRepository.save(currentUser);
    }
    
    @Override
    public UserResponseDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
        return convertToDto(user);
    }
}