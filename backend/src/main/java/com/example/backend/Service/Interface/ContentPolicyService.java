package com.example.backend.Service.Interface;

import com.example.backend.DTO.Request.AcceptContentPolicyRequestDTO;

import java.time.LocalDateTime;
import java.util.Map;

public interface ContentPolicyService {

    /**
     * Ghi nhận việc người dùng hiện tại đồng ý Quy tắc cộng đồng.
     *
     * @return map gồm version đã lưu và thời điểm chấp thuận
     */
    Map<String, Object> acceptContentPolicy(AcceptContentPolicyRequestDTO request);

    /** Phiên bản quy tắc hệ thống đang áp dụng (cấu hình trong properties). */
    String getCurrentPolicyVersion();

    /** Thời điểm người dùng hiện tại đã đồng ý, null nếu chưa. */
    LocalDateTime getMyAcceptedAt();
}