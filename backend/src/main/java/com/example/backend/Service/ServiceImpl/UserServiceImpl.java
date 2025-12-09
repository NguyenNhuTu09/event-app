package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.ChangePasswordRequestDTO;
import com.example.backend.DTO.Request.ForgotPasswordRequestDTO;
import com.example.backend.DTO.Request.ResetPasswordRequestDTO;
import com.example.backend.DTO.UserResponseDTO;
import com.example.backend.DTO.UserUpdateDTO;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.EmailService;
import com.example.backend.Service.Interface.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // private final S3Service s3Service;
    private final EmailService emailService;

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
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + userEmail));
        return convertToDto(user);
    }

    @Override
    public UserResponseDTO updateCurrentUserProfile(UserUpdateDTO userUpdateDTO) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User userToUpdate = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + userEmail));
        
        userToUpdate.setUsername(userUpdateDTO.getUsername());
        userToUpdate.setAddress(userUpdateDTO.getAddress());
        userToUpdate.setGender(userUpdateDTO.getGender());
        userToUpdate.setDateOfBirth(userUpdateDTO.getDateOfBirth());
        userToUpdate.setPhoneNumber(userUpdateDTO.getPhoneNumber());
        userToUpdate.setAvatarUrl(userUpdateDTO.getAvatarUrl());

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

    // @Override
    // public void changeCurrentUserPassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
    //     String user = SecurityContextHolder.getContext().getAuthentication().getName();
    //     User currentUser = userRepository.findByEmail(user)
    //             .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + user));
    //     if (!passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), currentUser.getPassword())) {
    //         throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
    //     }
    //     if (!changePasswordRequestDTO.getNewPassword().equals(changePasswordRequestDTO.getConfirmPassword())) {
    //         throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu không trùng khớp.");
    //     }
    //     String encodedNewPassword = passwordEncoder.encode(changePasswordRequestDTO.getNewPassword());
    //     currentUser.setPassword(encodedNewPassword);
    //     userRepository.save(currentUser);
    // }
    
    @Override
    public UserResponseDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
        return convertToDto(user);
    }

    @Override
    public void changeCurrentUserPassword(ChangePasswordRequestDTO changePasswordRequestDTO) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + userEmail));

        if (!passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác.");
        }
        if (!changePasswordRequestDTO.getNewPassword().equals(changePasswordRequestDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận không trùng khớp.");
        }

        currentUser.setPassword(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()));
        userRepository.save(currentUser);

        emailService.sendNotificationEmail(
            currentUser.getEmail(), 
            "Thông báo: Mật khẩu đã thay đổi", 
            currentUser.getUsername()
        );
    }

    @Override
    public void forgotPassword(ForgotPasswordRequestDTO forgotPasswordDTO) {
        User user = userRepository.findByEmail(forgotPasswordDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email không tồn tại trong hệ thống."));

        String otp = String.format("%06d", new Random().nextInt(999999));

        user.setResetPasswordToken(otp);
        user.setTokenExpiryDate(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        String subject = "Mã xác nhận quên mật khẩu";
        // emailService.sendSimpleMail(user.getEmail(), body, subject);
        emailService.sendEmailWithTemplate(user.getEmail(), subject, user.getUsername(), otp);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDTO resetPasswordDTO) {
        User user = userRepository.findByEmail(resetPasswordDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email không tồn tại."));

        if (user.getResetPasswordToken() == null || !user.getResetPasswordToken().equals(resetPasswordDTO.getOtp())) {
            throw new IllegalArgumentException("Mã OTP không chính xác.");
        }

        if (user.getTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn. Vui lòng yêu cầu lại.");
        }

        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không trùng khớp.");
        }

        user.setPassword(passwordEncoder.encode(resetPasswordDTO.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setTokenExpiryDate(null);
        
        userRepository.save(user);
    }
}