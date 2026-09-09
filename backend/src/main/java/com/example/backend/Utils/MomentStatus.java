package com.example.backend.Utils;

/**
 * Trạng thái kiểm duyệt của một khoảnh khắc (moment).
 *
 * VISIBLE      - hiển thị bình thường trong feed.
 * UNDER_REVIEW - đang chờ admin xem xét; ẩn khỏi feed công khai,
 *                chỉ chủ bài viết còn thấy (kèm nhãn "đang kiểm duyệt").
 * REMOVED      - admin đã gỡ; không trả về cho bất kỳ ai.
 */
public enum MomentStatus {
    VISIBLE,
    UNDER_REVIEW,
    REMOVED
}