package com.example.backend.Service;

import java.time.Instant;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.Request.LoginRequest;
import com.example.backend.DTO.Request.RegistrationRequest;
import com.example.backend.DTO.Request.TokenExchangeRequest;
import com.example.backend.DTO.Response.JwtAuthenticationResponse;
import com.example.backend.DTO.UserResponseDTO;
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
    private final OneTimeCodeService oneTimeCodeService;
    private final JwtService jwtService;
    
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

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
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng. Vui lòng đăng nhập.");
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
    public User processOAuthPostLogin(String email, String name, String avatarUrl) {
        return userRepository.findByEmail(email)
            .map(existingUser -> {
                existingUser.setAvatarUrl(avatarUrl);
                if (existingUser.getUsername() == null || existingUser.getUsername().isEmpty()) {
                    existingUser.setUsername(name);
                }
                
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(name);
                newUser.setAvatarUrl(avatarUrl);
                newUser.setRole(Role.USER);
                newUser.setProvider(AuthProvider.GOOGLE);
                newUser.setEnabled(true);
                newUser.setUid(java.util.UUID.randomUUID().toString()); 
                
                return userRepository.save(newUser);
            });
    }
    
    public JwtAuthenticationResponse exchangeCodeForJwt(TokenExchangeRequest request) {
        String code = request.getRefreshToken(); 
        String email = oneTimeCodeService.getEmailForCode(code);
        if (email == null) {
            throw new RuntimeException("Mã không hợp lệ hoặc đã hết hạn.");
        }
        
        // Lấy User
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
        
        // Tạo Access Token
        String accessToken = jwtService.generateToken(user.getEmail());
        
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        UserResponseDTO userDto = new UserResponseDTO(
            user.getUid(), user.getUsername(), user.getEmail(), 
            user.getAddress(), user.getGender(), user.getDateOfBirth(), 
            user.getPhoneNumber(), user.getAvatarUrl(), user.getRole()
        );

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user sau khi xác thực"));

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        UserResponseDTO userDto = new UserResponseDTO(
            user.getUid(), user.getUsername(), user.getEmail(), 
            user.getAddress(), user.getGender(), user.getDateOfBirth(), 
            user.getPhoneNumber(), user.getAvatarUrl(), user.getRole()
        );

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }
    

    public JwtAuthenticationResponse refreshToken(TokenExchangeRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        User user = userRepository.findByRefreshToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token không tồn tại hoặc không chính xác!"));

        if (user.getRefreshTokenExpiryDate().compareTo(Instant.now()) < 0) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiryDate(null);
            userRepository.save(user);
            throw new RuntimeException("Refresh Token đã hết hạn. Vui lòng đăng nhập lại.");
        }

        String newAccessToken = jwtService.generateToken(user.getEmail());

        String newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        UserResponseDTO userDto = new UserResponseDTO(
            user.getUid(), user.getUsername(), user.getEmail(), 
            user.getAddress(), user.getGender(), user.getDateOfBirth(), 
            user.getPhoneNumber(), user.getAvatarUrl(), user.getRole()
        );

        return JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken) 
                .user(userDto)
                .build();
    }
      
   
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        user.setRefreshToken(null);
        user.setRefreshTokenExpiryDate(null);
        userRepository.save(user);
    }
}