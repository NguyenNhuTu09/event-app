package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.AcceptContentPolicyRequestDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.Interface.ContentPolicyService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentPolicyServiceImpl implements ContentPolicyService {

    private final UserRepository userRepository;

    /**
     * Phiên bản quy tắc hệ thống đang áp dụng. Khi bạn cập nhật nội dung Quy
     * tắc cộng đồng thì tăng giá trị này trong application.properties:
     *
     *   app.content-policy.version=1.1
     *
     * Client so sánh contentPolicyAcceptedVersion trong GET /users/me với
     * phiên bản nó đang hiển thị; khác nhau thì bắt người dùng đồng ý lại.
     */
    @Value("${app.content-policy.version:1.0}")
    private String currentPolicyVersion;

    @Override
    @Transactional
    public Map<String, Object> acceptContentPolicy(AcceptContentPolicyRequestDTO request) {
        User me = getCurrentUser();
        String version = request.getVersion().trim();

        // Giữ nguyên mốc thời gian của lần đồng ý đầu tiên với cùng một phiên
        // bản. Client gọi endpoint này theo kiểu best-effort nên có thể gửi
        // lại nhiều lần; ghi đè mỗi lần sẽ làm mất bằng chứng người dùng đã
        // đồng ý từ khi nào — thứ cần trưng ra nếu Google hỏi.
        boolean sameVersion = version.equals(me.getContentPolicyAcceptedVersion());

        if (!sameVersion) {
            me.setContentPolicyAcceptedVersion(version);
            me.setContentPolicyAcceptedAt(LocalDateTime.now());
            userRepository.save(me);

            log.info("CONTENT_POLICY accepted userId={} version={}", me.getId(), version);
        }

        if (!version.equals(currentPolicyVersion)) {
            // Không chặn: client có thể đang chạy bản cũ chưa cập nhật.
            // Nhưng ghi log để biết còn bao nhiêu người dùng bản quy tắc cũ.
            log.warn("CONTENT_POLICY userId={} đồng ý phiên bản {} trong khi hệ thống đang dùng {}",
                    me.getId(), version, currentPolicyVersion);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("version", me.getContentPolicyAcceptedVersion());
        result.put("acceptedAt", me.getContentPolicyAcceptedAt());
        result.put("currentVersion", currentPolicyVersion);
        return result;
    }

    @Override
    public String getCurrentPolicyVersion() {
        return currentPolicyVersion;
    }

    @Override
    public LocalDateTime getMyAcceptedAt() {
        return getCurrentUser().getContentPolicyAcceptedAt();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại."));
    }
}