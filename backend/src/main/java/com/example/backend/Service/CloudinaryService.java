package com.example.backend.Service;
import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        String transformationString = new Transformation<>()
                .width(1920).crop("limit") // Chỉ resize nếu ảnh lớn hơn 1920px
                .quality("auto")           // Tự động nén ảnh (quan trọng để nhẹ file)
                .fetchFormat("auto")       // Tự động chọn định dạng tối ưu (webp/avif)
                .generate();

        Map params = ObjectUtils.asMap(
            "folder", "event_app/uploads",
            "resource_type", "image",
            "transformation", transformationString
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult.get("secure_url").toString();
    }
}