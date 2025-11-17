package com.example.backend.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.UserRepository;

@Service
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final long refreshTokenDurationMs;

    public RefreshTokenService(UserRepository userRepository, 
                               @Value("${jwt.refreshToken.expiration}") long refreshTokenDurationMs) {
        this.userRepository = userRepository;
        this.refreshTokenDurationMs = refreshTokenDurationMs;
    }

    @Transactional
    public String createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        String token = UUID.randomUUID().toString();
        user.setRefreshToken(token);
        user.setRefreshTokenExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        
        userRepository.save(user);
        return token;
    }

    @Transactional
    public Optional<User> findByToken(String token) {
        return userRepository.findByRefreshToken(token);
    }

    @Transactional
    public User verifyExpiration(User user) {
        if (user.getRefreshTokenExpiryDate().compareTo(Instant.now()) < 0) {
            user.setRefreshToken(null);
            user.setRefreshTokenExpiryDate(null);
            userRepository.save(user);
            throw new RuntimeException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return user;
    }
}
