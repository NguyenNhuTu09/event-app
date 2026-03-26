package com.example.backend.Controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.Service.CloudinaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/images")
@Tag(name = "File Upload Management")
public class ImageUploadController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Operation(summary = "Tải lên hình ảnh/video cho trình soạn thảo")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImageForEditor(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", 0)); 
            }
            
            String fileUrl = cloudinaryService.uploadMedia(file); 
            
            return ResponseEntity.ok(Map.of(
                "success", 1,
                "file", Map.of("url", fileUrl) // Trả về url như cũ cho Editor (CKEditor / EditorJS)
            ));
        } catch (IOException e) {
            return ResponseEntity.ok(Map.of("success", 0));
        }
    }
}