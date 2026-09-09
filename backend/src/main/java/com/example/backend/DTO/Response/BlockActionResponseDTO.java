package com.example.backend.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload của POST /users/{userId}/block — đúng hình dạng client mobile mong đợi. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockActionResponseDTO {
    private Long blockedUserId;
}