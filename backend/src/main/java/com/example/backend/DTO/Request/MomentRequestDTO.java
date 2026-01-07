package com.example.backend.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomentRequestDTO {
    private String caption;
    private String imageUrl; 
}
