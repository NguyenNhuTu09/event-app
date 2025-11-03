package com.example.backend.Service;

import java.util.List;

import com.example.backend.DTO.UserResponseDTO;
import com.example.backend.DTO.UserUpdateDTO;

public interface UserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO getCurrentUserProfile();
    UserResponseDTO updateCurrentUserProfile(UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
}