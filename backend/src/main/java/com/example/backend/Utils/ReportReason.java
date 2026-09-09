package com.example.backend.Utils;

/**
 * Lý do báo cáo nội dung. Danh sách phải khớp đúng với client mobile.
 *
 * CSAE = Child Sexual Abuse and Exploitation — Google yêu cầu xử lý
 * riêng: chỉ cần 1 báo cáo là ẩn bài ngay lập tức.
 */
public enum ReportReason {
    SEXUAL_CONTENT,
    NUDITY,
    VIOLENCE,
    HARASSMENT,
    HATE_SPEECH,
    SPAM,
    CSAE,
    OTHER
}