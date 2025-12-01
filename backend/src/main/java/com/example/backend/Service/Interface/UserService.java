package com.example.backend.Service.Interface;

import java.util.List;

import com.example.backend.DTO.ChangePasswordRequestDTO;
import com.example.backend.DTO.UserResponseDTO;
import com.example.backend.DTO.UserUpdateDTO;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO getCurrentUserProfile();
    UserResponseDTO updateCurrentUserProfile(UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
    void changeCurrentUserPassword(ChangePasswordRequestDTO changePasswordRequestDTO);
    UserResponseDTO findUserByEmail(String email);
    // UserResponseDTO updateCurrentUserAvatar(MultipartFile file) throws IOException;
}