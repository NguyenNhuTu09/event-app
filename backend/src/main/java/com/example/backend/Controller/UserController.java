package com.example.backend.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.ChangePasswordRequestDTO;
import com.example.backend.DTO.UserResponseDTO;
import com.example.backend.DTO.UserUpdateDTO;
import com.example.backend.Service.Interface.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management") 
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lấy danh sách tất cả người dùng (SADMIN)")
    @GetMapping
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @Operation(summary = "Lấy thông tin người dùng theo ID (SADMIN)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Cập nhật thông tin người dùng hiện tại")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> updateCurrentUserProfile(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(userUpdateDTO));
    }

    @Operation(summary = "Xóa người dùng theo ID (SADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Người dùng với ID " + id + " đã được xóa thành công.");
    }

    @Operation(summary = "Thay đổi mật khẩu của người dùng đang đăng nhập")
    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changeCurrentUserPassword(@Valid @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO) {
        try {
            userService.changeCurrentUserPassword(changePasswordRequestDTO);
            return ResponseEntity.ok("Đổi mật khẩu thành công.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Tìm người dùng theo email (SADMIN)")
    @GetMapping("/search") 
    @PreAuthorize("hasAuthority('SADMIN')")
    public ResponseEntity<UserResponseDTO> findUserByEmail(
            @Parameter(description = "Email của người dùng cần tìm", required = true, example = "jane.doe@example.com")
            @RequestParam String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }


    // @PostMapping("/me/avatar")
    // @Operation(summary = "Cập nhật ảnh đại diện cho người dùng hiện tại")
    // public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
    //     try {
    //         if (file.isEmpty()) {
    //             return new ResponseEntity<>("File ảnh không được để trống.", HttpStatus.BAD_REQUEST);
    //         }
    //         UserResponseDTO updatedUser = userService.updateCurrentUserAvatar(file);
    //         return ResponseEntity.ok(updatedUser);
    //     } catch (IOException e) {
    //         return new ResponseEntity<>("Tải ảnh lên thất bại: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    //     } catch (Exception e) {
    //         return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    //     }
    // }
}