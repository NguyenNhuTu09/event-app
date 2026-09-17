package com.example.backend.Service.Listener;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Kiểm tra một URL media còn được dùng ở bất kỳ đâu trong DB hay không.
 *
 * VÌ SAO CẦN
 * ==========
 * Backend không lưu ai là người upload file: /api/images/upload chỉ trả URL,
 * còn avatarUrl (PUT /api/users/me) và imageUrl của moment là chuỗi client tự
 * gửi lên. Nếu xoá file chỉ dựa vào "URL nằm trong dữ liệu của user", một
 * người có thể đặt avatar = URL banner của sự kiện khác, xoá tài khoản, và
 * banner đó biến mất khỏi Cloudinary.
 *
 * Vì vậy chỉ xoá file khi SAU KHI COMMIT không còn dòng nào khác tham chiếu.
 *
 * DUY TRÌ
 * =======
 * Thêm cột mới chứa URL media (ảnh, video, hoặc rich text có nhúng ảnh) ở bất
 * kỳ bảng nào thì PHẢI bổ sung vào câu SQL dưới đây. Thiếu cột nào thì file
 * được tham chiếu từ cột đó có thể bị xoá nhầm.
 *
 * Mọi lỗi truy vấn (ví dụ sai tên cột) đều coi như URL CÒN được dùng -> không
 * xoá. Sai theo hướng giữ file thừa, không bao giờ theo hướng xoá nhầm.
 */
@Component
@Slf4j
public class MediaReferenceChecker {

    @PersistenceContext
    private EntityManager entityManager;

    // INSTR thay cho LIKE: URL Cloudinary chứa '_' (event_app/...), mà '_' là
    // ký tự đại diện của LIKE.
    private static final String COUNT_REFERENCES_SQL = """
            SELECT
                (SELECT COUNT(*) FROM event_moments     WHERE image_url = :url)
              + (SELECT COUNT(*) FROM users             WHERE avatar_url = :url)
              + (SELECT COUNT(*) FROM events            WHERE banner_image_url = :url
                                                           OR INSTR(description, :url) > 0)
              + (SELECT COUNT(*) FROM organizers        WHERE logo_url = :url
                                                           OR INSTR(description, :url) > 0)
              + (SELECT COUNT(*) FROM presenters        WHERE avatar_url = :url
                                                           OR INSTR(bio, :url) > 0)
              + (SELECT COUNT(*) FROM activity          WHERE activity_image_url = :url
                                                           OR INSTR(description, :url) > 0)
              + (SELECT COUNT(*) FROM posts             WHERE thumbnail_url = :url)
              + (SELECT COUNT(*) FROM post_translations WHERE INSTR(content, :url) > 0
                                                           OR INSTR(summary, :url) > 0)
              + (SELECT COUNT(*) FROM moment_reports    WHERE moment_image_url = :url)
            """;

    /** true nếu URL còn được dùng, hoặc nếu không kiểm tra được. */
    public boolean isStillReferenced(String url) {
        try {
            Object result = entityManager.createNativeQuery(COUNT_REFERENCES_SQL)
                    .setParameter("url", url)
                    .getSingleResult();
            return ((Number) result).longValue() > 0;
        } catch (Exception e) {
            log.error("MEDIA_REF không kiểm tra được tham chiếu, giữ nguyên file url={}: {}", url, e.getMessage());
            return true;
        }
    }
}