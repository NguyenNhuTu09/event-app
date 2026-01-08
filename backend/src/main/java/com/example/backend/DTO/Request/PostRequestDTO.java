package com.example.backend.DTO.Request;

import com.example.backend.Utils.PostStatus;

import lombok.Data;

@Data
public class PostRequestDTO {
    private String title;
    private String summary;
    private String content; 
    private String thumbnailUrl;
    private PostStatus status; 
}
