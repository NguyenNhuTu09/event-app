package com.example.backend.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CloudinaryService {

    private Cloudinary cloudinary;

    /** Thư mục uploadMedia() đang dùng. Chỉ file trong thư mục này mới được xoá. */
    private static final String UPLOAD_FOLDER = "event_app/uploads";

    /** Giới hạn public_id cho mỗi lần gọi Admin API delete_resources. */
    private static final int DELETE_BATCH_SIZE = 100;

    /**
     * secure_url dạng:
     *   https://res.cloudinary.com/{cloud}/{image|video|raw}/upload/[transformations/]v{version}/{public_id}.{ext}
     * Bắt buộc có đoạn version: không có thì không phân biệt được transformation
     * với thư mục, nên bỏ qua cho an toàn.
     */
    private static final Pattern DELIVERY_URL = Pattern.compile(
            "^https?://res\\.cloudinary\\.com/([^/]+)/(image|video|raw)/upload/(?:[^/]+/)*?v\\d+/(.+)$");

    public String uploadMedia(MultipartFile file) throws IOException {
        // Kiểm tra loại file (MIME type)
        String contentType = file.getContentType();
        boolean isVideo = contentType != null && contentType.startsWith("video/");

        // Khởi tạo params map
        Map<String, Object> params = new HashMap<>();
        params.put("folder", UPLOAD_FOLDER);
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

    /**
     * Xoá file theo secure_url đã lưu trong DB. Best-effort: không bao giờ ném
     * exception, lỗi được ghi log.
     *
     * Chỉ xử lý URL thuộc đúng cloud đang cấu hình VÀ nằm trong UPLOAD_FOLDER;
     * mọi URL khác (avatar Google, logo trong template email, cloud khác...)
     * đều bị bỏ qua.
     *
     * KHÔNG tự kiểm tra URL còn được dùng ở đâu khác — nơi gọi phải làm việc
     * đó trước (xem MediaReferenceChecker).
     *
     * @return số file Cloudinary xác nhận đã xoá
     */
    public int deleteByUrls(Collection<String> urls) {
        Map<String, List<String>> publicIdsByType = new HashMap<>();

        for (String url : urls) {
            Optional<ManagedAsset> asset = parseManagedAsset(url);
            if (asset.isEmpty()) {
                log.debug("CLOUDINARY bỏ qua URL không thuộc thư mục upload của hệ thống: {}", url);
                continue;
            }
            publicIdsByType
                    .computeIfAbsent(asset.get().resourceType(), k -> new ArrayList<>())
                    .add(asset.get().publicId());
        }

        int deleted = 0;
        for (Map.Entry<String, List<String>> entry : publicIdsByType.entrySet()) {
            List<String> ids = entry.getValue();
            for (int from = 0; from < ids.size(); from += DELETE_BATCH_SIZE) {
                List<String> batch = ids.subList(from, Math.min(from + DELETE_BATCH_SIZE, ids.size()));
                deleted += deleteBatch(entry.getKey(), batch);
            }
        }
        return deleted;
    }

    private int deleteBatch(String resourceType, List<String> publicIds) {
        try {
            Map<?, ?> response = cloudinary.api().deleteResources(
                    publicIds,
                    ObjectUtils.asMap("resource_type", resourceType, "invalidate", true));

            // "deleted": { "<public_id>": "deleted" | "not_found" }
            Object result = response.get("deleted");
            if (!(result instanceof Map<?, ?> perId)) {
                return 0;
            }
            return (int) perId.values().stream().filter("deleted"::equals).count();
        } catch (Exception e) {
            log.error("CLOUDINARY xoá thất bại resourceType={} count={}: {}",
                    resourceType, publicIds.size(), e.getMessage());
            return 0;
        }
    }

    Optional<ManagedAsset> parseManagedAsset(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }

        String withoutQuery = url.split("[?#]", 2)[0];
        Matcher matcher = DELIVERY_URL.matcher(withoutQuery);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String cloudName = matcher.group(1);
        String resourceType = matcher.group(2);
        String path = matcher.group(3);

        if (!cloudName.equals(cloudinary.config.cloudName)) {
            return Optional.empty();
        }
        if (!path.startsWith(UPLOAD_FOLDER + "/")) {
            return Optional.empty();
        }

        // Với image / video, public_id không kèm phần mở rộng; với raw thì có.
        String publicId = path;
        if (!"raw".equals(resourceType)) {
            int lastSlash = path.lastIndexOf('/');
            int lastDot = path.lastIndexOf('.');
            if (lastDot > lastSlash) {
                publicId = path.substring(0, lastDot);
            }
        }

        return Optional.of(new ManagedAsset(resourceType, publicId));
    }

    record ManagedAsset(String resourceType, String publicId) {
    }
}