package com.example.backend.Controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.AcceptContentPolicyRequestDTO;
import com.example.backend.Service.Interface.ContentPolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Ghi nhận việc người dùng đồng ý Quy tắc cộng đồng trước lần đăng bài đầu tiên.
 *
 * Client mobile lưu trạng thái ở AsyncStorage và gọi endpoint này theo kiểu
 * best-effort — lỗi ở đây không chặn luồng đăng bài. Endpoint tồn tại để có
 * bằng chứng phía server rằng đã thu thập chấp thuận, thứ Google có thể hỏi.
 *
 * Tách controller riêng để không phải sửa UserController. Đường dẫn
 * "/api/users/me/accept-content-policy" có 4 đoạn nên không đụng
 * "/api/users/{uid}" đang có.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Content Policy")
@SecurityRequirement(name = "bearerAuth")
public class ContentPolicyController {

    private final ContentPolicyService contentPolicyService;

    @Operation(summary = "Ghi nhận người dùng đồng ý Quy tắc cộng đồng")
    @PostMapping("/me/accept-content-policy")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> acceptContentPolicy(
            @Valid @RequestBody AcceptContentPolicyRequestDTO request) {
        return ResponseEntity.ok(contentPolicyService.acceptContentPolicy(request));
    }

    @Operation(summary = "Phiên bản Quy tắc cộng đồng hệ thống đang áp dụng")
    @GetMapping("/content-policy/version")
    @SecurityRequirements()
    public ResponseEntity<Map<String, Object>> getCurrentVersion() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("currentVersion", contentPolicyService.getCurrentPolicyVersion());
        return ResponseEntity.ok(body);
    }
}