package com.example.backend.Service.ServiceImpl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Response.BlockActionResponseDTO;
import com.example.backend.DTO.Response.BlockedUserResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.User;
import com.example.backend.Models.Entity.UserBlock;
import com.example.backend.Repository.UserBlockRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.Interface.UserBlockService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserBlockServiceImpl implements UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BlockActionResponseDTO blockUser(Long targetUserId) {
        User me = getCurrentUser();

        if (me.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("Bạn không thể tự chặn chính mình.");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng cần chặn."));

        // Idempotent: client mobile có thể gửi lại request khi mạng chập chờn,
        // hoặc người dùng bấm chặn hai lần. Trả về thành công thay vì 409 để
        // app không phải xử lý thêm trường hợp lỗi cho một hành động vô hại.
        if (!userBlockRepository.existsByBlocker_IdAndBlocked_Id(me.getId(), targetUserId)) {
            UserBlock block = UserBlock.builder()
                    .blocker(me)
                    .blocked(target)
                    .build();
            userBlockRepository.save(block);

            log.info("MODERATION block blockerId={} blockedId={}", me.getId(), targetUserId);
        }

        return BlockActionResponseDTO.builder()
                .blockedUserId(targetUserId)
                .build();
    }

    @Override
    @Transactional
    public void unblockUser(Long targetUserId) {
        User me = getCurrentUser();

        // Cũng idempotent: bỏ chặn người chưa từng chặn vẫn trả 204.
        // deleteBy... không ném lỗi khi không có dòng nào khớp.
        userBlockRepository.deleteByBlocker_IdAndBlocked_Id(me.getId(), targetUserId);

        log.info("MODERATION unblock blockerId={} blockedId={}", me.getId(), targetUserId);
    }

    @Override
    public Page<BlockedUserResponseDTO> getMyBlocks(Pageable pageable) {
        User me = getCurrentUser();

        return userBlockRepository.findBlockedUsers(me.getId(), pageable)
                .map(block -> BlockedUserResponseDTO.builder()
                        .userId(block.getBlocked().getId())
                        .username(block.getBlocked().getUsername())
                        .avatarUrl(block.getBlocked().getAvatarUrl())
                        .blockedAt(block.getCreatedAt())
                        .build());
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại."));
    }
}