package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.MomentReport;
import com.example.backend.Models.Entity.User;
import com.example.backend.Utils.ReportStatus;

public interface MomentReportRepository extends JpaRepository<MomentReport, Long> {

    // =================================================================
    // Dùng cho luồng báo cáo của người dùng (§1)
    // =================================================================

    /** Chặn báo cáo trùng — dùng trước khi insert để trả 409 sạch sẽ,
     *  thay vì để DataIntegrityViolationException làm hỏng transaction. */
    boolean existsByMoment_IdAndReporter_Id(Long momentId, Long reporterId);

    /**
     * Đếm số báo cáo còn hiệu lực của một moment (bỏ qua các báo cáo đã bị
     * admin bác). Vì đã có UNIQUE (moment_id, reporter_id) nên số dòng
     * chính là số người báo cáo khác nhau.
     */
    long countByMoment_IdAndStatusNot(Long momentId, ReportStatus status);

    /**
     * Gỡ liên kết báo cáo khỏi moment sắp bị xoá cứng.
     * Phải gọi TRƯỚC khi xoá moment, nếu không sẽ vi phạm khoá ngoại và
     * job dọn dẹp lúc 2h sáng sẽ chết âm thầm.
     */
    @Modifying
    @Query("UPDATE MomentReport r SET r.moment = null WHERE r.moment.id IN :momentIds")
    int detachFromMoments(@Param("momentIds") List<Long> momentIds);

    @Modifying
    @Query("UPDATE MomentReport r SET r.moment = null WHERE r.moment.id = :momentId")
    int detachFromMoment(@Param("momentId") Long momentId);

    // =================================================================
    // Dùng cho công cụ admin (§5)
    //
    // Tách thành hai method thay vì một query có "(:status IS NULL OR ...)":
    // truyền enum null vào so sánh IS NULL hay gặp lỗi suy luận kiểu ở
    // Hibernate, và hai câu tường minh cũng dễ đọc hơn.
    // =================================================================

    @Query(value = """
            SELECT r FROM MomentReport r
            JOIN FETCH r.reporter
            LEFT JOIN FETCH r.moment m
            LEFT JOIN FETCH m.user
            WHERE r.status = :status
            """,
            countQuery = "SELECT COUNT(r) FROM MomentReport r WHERE r.status = :status")
    Page<MomentReport> findByStatusForAdmin(@Param("status") ReportStatus status, Pageable pageable);

    @Query(value = """
            SELECT r FROM MomentReport r
            JOIN FETCH r.reporter
            LEFT JOIN FETCH r.moment m
            LEFT JOIN FETCH m.user
            """,
            countQuery = "SELECT COUNT(r) FROM MomentReport r")
    Page<MomentReport> findAllForAdmin(Pageable pageable);

    /**
     * Đóng toàn bộ báo cáo đang chờ của một moment sau khi admin ra quyết định.
     *
     * Bulk update nên bỏ qua persistence context: gọi xong thì các entity
     * MomentReport đang nạp trong cùng transaction sẽ lỗi thời. Trong service
     * hiện tại không dùng lại chúng nên an toàn; nếu sau này có, nhớ
     * flush/clear hoặc đọc lại.
     */
    @Modifying
    @Query("""
            UPDATE MomentReport r
               SET r.status = :newStatus,
                   r.resolvedAt = :resolvedAt,
                   r.resolvedBy = :admin,
                   r.resolutionNote = :note
             WHERE r.moment.id = :momentId
               AND r.status = com.example.backend.Utils.ReportStatus.PENDING
            """)
    int closePendingReports(@Param("momentId") Long momentId,
                            @Param("newStatus") ReportStatus newStatus,
                            @Param("admin") User admin,
                            @Param("resolvedAt") LocalDateTime resolvedAt,
                            @Param("note") String note);

    /** Số báo cáo đang chờ — hiển thị trên dashboard admin. */
    long countByStatus(ReportStatus status);
}