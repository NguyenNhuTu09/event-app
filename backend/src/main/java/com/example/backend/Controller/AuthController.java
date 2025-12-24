package com.example.backend.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.LoginRequest;
import com.example.backend.DTO.Request.RegistrationRequest;
import com.example.backend.DTO.Request.TokenExchangeRequest;
import com.example.backend.DTO.Request.VerifyAccountRequest;
import com.example.backend.DTO.Response.JwtAuthenticationResponse;
import com.example.backend.Models.Entity.User;
import com.example.backend.Service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng ký người dùng mới")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        try {
            User newUser = authService.registerNewUser(registrationRequest);
            return new ResponseEntity<>("Kiểm tra Email của bạn để xác thực tài khoản: " + newUser.getUsername(), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Đăng nhập")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/token/exchange")
    @SecurityRequirements()
    @Operation(summary = "Đổi mã dùng một lần lấy JWT Token sau khi đăng nhập OAuth2")
    public ResponseEntity<?> exchangeCodeForToken(@RequestBody TokenExchangeRequest tokenExchangeRequest) {
        try {
            JwtAuthenticationResponse jwtResponse = authService.exchangeCodeForJwt(tokenExchangeRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Làm mới Access Token bằng Refresh Token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenExchangeRequest tokenExchangeRequest) {
        try {
            JwtAuthenticationResponse response = authService.refreshToken(tokenExchangeRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "Đăng xuất")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
             return ResponseEntity.badRequest().body("Error: User is not logged in.");
        }
        String email = "";
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        authService.logout(email);
        return ResponseEntity.ok("User logged out successfully!");
    }

    @PostMapping("/verify")
    @SecurityRequirements()
    @Operation(summary = "Xác thực tài khoản qua email")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyAccountRequest request) {
        try {
            if (authService.verifyUser(request.getEmail(), request.getVerificationCode())) {
                return ResponseEntity.ok("Xác thực tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
            } else {
                return ResponseEntity.badRequest().body("Mã xác thực không chính xác hoặc tài khoản đã được kích hoạt.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}