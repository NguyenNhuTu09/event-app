package com.example.backend.Service.ServiceImpl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.RegistrationRequest;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Utils.AuthProvider;
import com.example.backend.Utils.Role;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerNewUser(RegistrationRequest registrationRequest) {
        if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu và xác nhận mật khẩu không trùng khớp.");
        }
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new RuntimeException("Lỗi: Tên đăng nhập đã được sử dụng!");
        }
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Lỗi: Email đã được sử dụng!");
        }
        User newUser = new User();
        newUser.setUsername(registrationRequest.getUsername());
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setRole(Role.USER);
        newUser.setProvider(AuthProvider.LOCAL);
        return userRepository.save(newUser);
    }

    @Transactional
    public User processOAuthPostLogin(String email, String username, String avatarUrl) {
        return userRepository.findByEmail(email)
            .map(existingUser -> {
                existingUser.setUsername(username); 
                existingUser.setAvatarUrl(avatarUrl);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(username);
                newUser.setEmail(email);
                newUser.setAvatarUrl(avatarUrl);
                newUser.setProvider(AuthProvider.GOOGLE);
                newUser.setRole(Role.USER); 
                newUser.setEnabled(true);
                return userRepository.save(newUser);
            });
    }
}