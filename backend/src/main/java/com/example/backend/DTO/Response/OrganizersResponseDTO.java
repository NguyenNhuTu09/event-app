package com.example.backend.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizersResponseDTO {
    private Integer organizerId;
    private String slug;
    private String name;
    private String description;
    private String logoUrl;
    private String contactPhoneNumber;
    private String contactEmail;
    private boolean isApproved;
    private String unlockRequestReason;

    private Long userId;
    private String username; 

    private boolean isLocked;
    private boolean isUnlockRequested;
}
