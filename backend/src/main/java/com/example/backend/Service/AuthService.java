package com.example.backend.Service;

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
import com.example.backend.DTO.Response.JwtResponse;
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
    public JwtResponse exchangeCodeForJwt(TokenExchangeRequest request) {
        String code = request.getRefreshToken();
        String email = oneTimeCodeService.getEmailForCode(code);
        
        if (email == null) {
            throw new RuntimeException("Mã không hợp lệ hoặc đã hết hạn.");
        }
        User userEntity = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));
        
        String jwt = jwtService.generateToken(email);
        return new JwtResponse(
            jwt,
            "Bearer",
            userEntity.getId(),
            userEntity.getUsername(),
            userEntity.getEmail()
        );
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
            user.getId(), user.getUsername(), user.getEmail(), 
            user.getAddress(), user.getGender(), user.getDateOfBirth(), 
            user.getPhoneNumber(), user.getAvatarUrl(), user.getRole()
        );

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }
    

    public JwtAuthenticationResponse exchangeCodeForTokens(TokenExchangeRequest request) {
        String code = request.getRefreshToken();
        String email = oneTimeCodeService.getEmailForCode(code);
        
        if (email == null) {
            throw new RuntimeException("Mã không hợp lệ hoặc đã hết hạn.");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với email: " + email));

        String accessToken = jwtService.generateToken(email);

        String refreshToken = refreshTokenService.createRefreshToken(email);
        
        UserResponseDTO userDto = new UserResponseDTO(
            user.getId(), user.getUsername(), user.getEmail(), 
            user.getAddress(), user.getGender(), user.getDateOfBirth(), 
            user.getPhoneNumber(), user.getAvatarUrl(), user.getRole()
        );

        return JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
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