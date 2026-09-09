package com.example.backend.Controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Response.BlockedUserResponseDTO;
import com.example.backend.Service.Interface.UserBlockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Tách riêng khỏi UserController để không phải sửa file đang có.
 *
 * Hai controller cùng base path "/api/users" là hợp lệ với Spring, miễn là
 * không có cặp (method, path) nào trùng nhau. Ở đây:
 *   - "/api/users/me/blocks" có 3 đoạn nên không đụng "/api/users/{uid}";
 *   - "/api/users/{userId}/block" khác cả về số đoạn lẫn HTTP method so với
 *     các endpoint quản trị theo {uid} đang có.
 *
 * Lưu ý {userId} ở đây là khoá chính dạng số (Long), khác với {uid} dạng UUID
 * chuỗi mà UserController đang dùng. Client mobile gửi userId số, đúng như
 * userId trả về trong MomentResponseDTO.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Block Management")
@SecurityRequirement(name = "bearerAuth")
public class UserBlockController {

    private final UserBlockService userBlockService;

    @Operation(summary = "Chặn một người dùng")
    @PostMapping("/{userId}/block")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> blockUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(userBlockService.blockUser(userId));
        } catch (IllegalArgumentException e) {
            // Tự chặn chính mình -> 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Bỏ chặn một người dùng")
    @DeleteMapping("/{userId}/block")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        userBlockService.unblockUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Danh sách người dùng tôi đã chặn")
    @GetMapping("/me/blocks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BlockedUserResponseDTO>> getMyBlocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(userBlockService.getMyBlocks(pageable));
    }
}