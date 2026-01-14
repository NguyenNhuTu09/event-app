package com.example.backend.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizerStatusResponseDTO {
    private boolean isApproved;       
    private boolean isLocked;         
    private boolean isUnlockRequested; 
    private String slug;              
    private String organizerName;
}
