package com.example.backend.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityEmailDTO {
    private String name;
    private String date;      
    private String timeRange; 
    private String location;
    private String description;
}
