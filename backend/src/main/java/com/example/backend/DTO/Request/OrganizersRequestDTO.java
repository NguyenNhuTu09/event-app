package com.example.backend.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizersRequestDTO {
    private String name;
    private String description;
    private String logoUrl;
    private String contactPhoneNumber;
    private String contactEmail;
}
