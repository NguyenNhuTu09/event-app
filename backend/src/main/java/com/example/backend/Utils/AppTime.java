package com.example.backend.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Giờ Việt Nam, dùng cho MỌI chỗ so sánh với thời gian do người dùng nhập.
 *
 * Hệ thống đang có hai loại LocalDateTime:
 *   1. Người dùng nhập  -> giờ VN  (events.start_date, activity.start_time...)
 *   2. Server tự sinh   -> giờ UTC (posted_at, created_at...) vì JVM chạy UTC
 *
 * LocalDateTime không mang múi giờ, Java vẫn so chúng như cùng một hệ.
 *
 * QUY TẮC:
 *   So với thời gian người dùng nhập -> AppTime.now()
 *   So với thời gian server tự sinh  -> LocalDateTime.now() như cũ
 */
public final class AppTime {

    public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private AppTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZONE);
    }

    public static LocalDate today() {
        return LocalDate.now(ZONE);
    }
}