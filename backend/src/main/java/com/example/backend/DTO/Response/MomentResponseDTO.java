package com.example.backend.DTO.Response;

import java.time.LocalDateTime;
import lombok.Builder; 
import lombok.Data;

@Data
@Builder 
public class MomentResponseDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userAvatar;
    private String caption;
    private String imageUrl;
    private LocalDateTime postedAt;
    private String timeAgo; 
}
