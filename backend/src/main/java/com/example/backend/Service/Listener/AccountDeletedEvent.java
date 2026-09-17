package com.example.backend.Service.Listener;

import java.util.List;

/**
 * Phát ra trong transaction xoá tài khoản; AccountDeletionListener xử lý SAU
 * KHI commit.
 *
 * Mang theo email / username GỐC vì lúc listener chạy, dòng users đã bị ẩn
 * danh hoá — đây là cách duy nhất để gửi email xác nhận tới đúng người.
 *
 * @param deletedMoments    các moment đã bị xoá, để phát WS "DELETE"
 * @param mediaUrlCandidates URL ảnh CÓ THỂ xoá (đã loại ảnh bằng chứng báo cáo
 *                          của chính user). Listener còn kiểm tra thêm rằng
 *                          không nơi nào khác trong DB đang dùng URL đó.
 */
public record AccountDeletedEvent(
        Long userId,
        String originalEmail,
        String originalUsername,
        boolean deletedByAdmin,
        List<MomentRef> deletedMoments,
        List<String> mediaUrlCandidates) {

    public record MomentRef(Long momentId, Long eventId) {
    }
}