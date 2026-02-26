package com.example.backend.DTO.Request;
import java.util.Map;
import com.example.backend.Utils.PostStatus;
import lombok.Data;

@Data
public class PostRequestDTO {
    private String thumbnailUrl;
    private PostStatus status; 
    
    private Map<String, PostTranslationRequestDTO> translations; 
}