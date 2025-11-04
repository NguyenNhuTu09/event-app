package com.example.backend.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.JwtResponse;
import com.example.backend.DTO.LoginRequest;
import com.example.backend.DTO.RegistrationRequest;
import com.example.backend.Models.Entity.User;
import com.example.backend.Service.ServiceImpl.AuthService;
import com.example.backend.Service.ServiceImpl.CustomUserDetailsService;
import com.example.backend.Service.ServiceImpl.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Operation(summary = "Đăng ký người dùng mới")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        try {
            User newUser = authService.registerNewUser(registrationRequest);
            return new ResponseEntity<>("Đăng ký thành công cho user: " + newUser.getUsername(), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Đăng nhập")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String jwt = jwtService.generateToken(userDetails.getUsername());
            User userEntity = userDetailsService.loadUserEntityByUsername(userDetails.getUsername());

            return ResponseEntity.ok(new JwtResponse(
                jwt,
                "Bearer",
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail()
            ));
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Email hoặc mật khẩu không hợp lệ!", HttpStatus.UNAUTHORIZED);
        }
    }
}