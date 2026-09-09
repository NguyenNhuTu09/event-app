package com.example.backend.DTO.Response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * Một dòng trong GET /users/me/blocks.
 *
 * Lưu ý về kiểu thời gian: spec của mobile ví dụ blockedAt dạng
 * "2026-09-03T10:00:00Z" (có hậu tố Z = UTC), còn toàn bộ dự án đang dùng
 * LocalDateTime nên JSON sẽ ra "2026-09-03T10:00:00" không có Z. Client phải
 * parse như giờ local, hoặc cả hai bên thống nhất đổi sang Instant —
 * nhưng đó là thay đổi ảnh hưởng mọi API, nên để nguyên cho nhất quán.
 */
@Data
@Builder
public class BlockedUserResponseDTO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private LocalDateTime blockedAt;
}