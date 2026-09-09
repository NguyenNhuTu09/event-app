package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.EventMoment;

public interface EventMomentRepository extends JpaRepository<EventMoment, Long> {

    // =================================================================
    // FEED CÔNG KHAI — đã lọc kiểm duyệt (§3)
    //
    // Ba điều kiện lọc, tất cả đều làm ở tầng DB:
    //   1. bỏ bài REMOVED;
    //   2. bỏ bài UNDER_REVIEW, TRỪ KHI người xem là chủ bài viết;
    //   3. bỏ bài của người mà người xem đã chặn.
    //
    // Điều kiện 3 dùng NOT EXISTS thay vì "NOT IN (:blockedIds)" là có chủ ý:
    // truyền một List rỗng vào mệnh đề IN sẽ sinh SQL không hợp lệ. NOT EXISTS
    // tránh hẳn trường hợp đó và không cần query danh sách chặn trước.
    // =================================================================

    @Query(value = """
            SELECT m FROM EventMoment m
            JOIN FETCH m.user u
            WHERE m.event.eventId = :eventId
              AND m.status <> com.example.backend.Utils.MomentStatus.REMOVED
              AND (m.status <> com.example.backend.Utils.MomentStatus.UNDER_REVIEW
                   OR u.id = :viewerId)
              AND NOT EXISTS (
                    SELECT 1 FROM UserBlock b
                    WHERE b.blocker.id = :viewerId
                      AND b.blocked.id = u.id)
            """,
            countQuery = """
            SELECT COUNT(m) FROM EventMoment m
            WHERE m.event.eventId = :eventId
              AND m.status <> com.example.backend.Utils.MomentStatus.REMOVED
              AND (m.status <> com.example.backend.Utils.MomentStatus.UNDER_REVIEW
                   OR m.user.id = :viewerId)
              AND NOT EXISTS (
                    SELECT 1 FROM UserBlock b
                    WHERE b.blocker.id = :viewerId
                      AND b.blocked.id = m.user.id)
            """)
    Page<EventMoment> findVisibleForViewer(@Param("eventId") Long eventId,
                                           @Param("viewerId") Long viewerId,
                                           Pageable pageable);

    @Query("""
            SELECT m FROM EventMoment m
            JOIN FETCH m.user u
            WHERE m.event.eventId = :eventId
              AND m.status <> com.example.backend.Utils.MomentStatus.REMOVED
              AND (m.status <> com.example.backend.Utils.MomentStatus.UNDER_REVIEW
                   OR u.id = :viewerId)
              AND NOT EXISTS (
                    SELECT 1 FROM UserBlock b
                    WHERE b.blocker.id = :viewerId
                      AND b.blocked.id = u.id)
            ORDER BY m.postedAt DESC
            """)
    List<EventMoment> findVisibleForViewer(@Param("eventId") Long eventId,
                                           @Param("viewerId") Long viewerId);

    // =================================================================
    // BÀI CỦA CHÍNH MÌNH — GET /moments/me
    // Vẫn trả về bài UNDER_REVIEW (client hiển thị nhãn "đang kiểm duyệt"),
    // nhưng ẩn bài đã bị admin gỡ.
    // =================================================================

    @Query("""
            SELECT m FROM EventMoment m
            JOIN FETCH m.user u
            WHERE m.event.eventId = :eventId
              AND u.id = :userId
              AND m.status <> com.example.backend.Utils.MomentStatus.REMOVED
            ORDER BY m.postedAt DESC
            """)
    List<EventMoment> findMyMoments(@Param("eventId") Long eventId,
                                    @Param("userId") Long userId);

    // =================================================================
    // Tra cứu & bảo trì
    // =================================================================

    /** Dùng cho endpoint report: đảm bảo moment thực sự thuộc event trên URL. */
    @Query("SELECT m FROM EventMoment m JOIN FETCH m.user WHERE m.id = :momentId AND m.event.eventId = :eventId")
    Optional<EventMoment> findByIdAndEventId(@Param("momentId") Long momentId,
                                             @Param("eventId") Long eventId);

    Optional<EventMoment> findByIdAndUser_Id(Long id, Long userId);

    @Query("SELECT m FROM EventMoment m WHERE m.event.endDate < :thresholdDate")
    List<EventMoment> findExpiredMoments(@Param("thresholdDate") LocalDateTime thresholdDate);

    @Modifying
    @Query("DELETE FROM EventMoment m WHERE m.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}