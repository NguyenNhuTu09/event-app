package com.example.backend.Service.Interface;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend.DTO.Response.BlockActionResponseDTO;
import com.example.backend.DTO.Response.BlockedUserResponseDTO;

public interface UserBlockService {

    /** Chặn một người dùng. Gọi lại khi đã chặn rồi thì không báo lỗi. */
    BlockActionResponseDTO blockUser(Long targetUserId);

    /** Bỏ chặn. Gọi trên người chưa từng bị chặn cũng không báo lỗi. */
    void unblockUser(Long targetUserId);

    /** Danh sách người mà tôi đã chặn. */
    Page<BlockedUserResponseDTO> getMyBlocks(Pageable pageable);
}