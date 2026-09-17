package com.example.backend.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.UserBlock;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    Optional<UserBlock> findByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    void deleteByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    /**
     * Danh sách người mà :blockerId đã chặn, kèm sẵn thông tin user.
     *
     * JOIN FETCH ở đây là cần thiết: quan hệ blocked là LAZY, nếu để Spring
     * tự sinh query thì mỗi dòng trong trang sẽ phát sinh thêm một câu SELECT
     * (N+1). Với size=100 như client đang gọi thì thành 101 query cho một
     * lần mở màn hình.
     */
    @Query(value = """
            SELECT b FROM UserBlock b
            JOIN FETCH b.blocked u
            WHERE b.blocker.id = :blockerId
            """,
            countQuery = "SELECT COUNT(b) FROM UserBlock b WHERE b.blocker.id = :blockerId")
    Page<UserBlock> findBlockedUsers(@Param("blockerId") Long blockerId, Pageable pageable);

    /**
     * Xoá tài khoản: xoá mọi quan hệ chặn có liên quan tới user, theo CẢ HAI
     * chiều (user chặn người khác, và người khác chặn user).
     *
     * Chiều "bị chặn" cũng phải xoá: nếu giữ, danh sách chặn của người kia sẽ
     * hiện một "deleted_user_..." không có nội dung nào để ẩn.
     *
     * Bulk delete (một câu SQL) thay cho deleteBy... dẫn xuất, vốn nạp từng
     * entity lên rồi mới xoá.
     */
    @Modifying
    @Query("DELETE FROM UserBlock b WHERE b.blocker.id = :userId OR b.blocked.id = :userId")
    int deleteAllInvolvingUser(@Param("userId") Long userId);
}