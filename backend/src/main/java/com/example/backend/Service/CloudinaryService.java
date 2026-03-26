package com.example.backend.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadMedia(MultipartFile file) throws IOException {
        // Kiểm tra loại file (MIME type)
        String contentType = file.getContentType();
        boolean isVideo = contentType != null && contentType.startsWith("video/");

        // Khởi tạo params map
        Map<String, Object> params = new HashMap<>();
        params.put("folder", "event_app/uploads");
        params.put("resource_type", "auto"); // QUAN TRỌNG: "auto" cho phép tải lên ảnh, video, audio

        // Chỉ áp dụng biến đổi (resize, optimize định dạng) nếu file đó LÀ ẢNH
        if (!isVideo) {
            String transformationString = new Transformation<>()
                    .width(1920).crop("limit") // Chỉ resize nếu ảnh lớn hơn 1920px
                    .quality("auto")           // Tự động nén ảnh
                    .fetchFormat("auto")       // Tự động chuyển đổi sang webp/avif
                    .generate();
            params.put("transformation", transformationString);
        } else {
            // (Tùy chọn) Đối với file video dung lượng lớn, Cloudinary khuyến cáo dùng chunk
            // Nếu video của bạn thường < 100MB thì đoạn dưới này không cần thiết,
            // dùng file.getBytes() mặc định vẫn chạy tốt.
        }

        // Thực hiện upload
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return uploadResult.get("secure_url").toString();
    }
}