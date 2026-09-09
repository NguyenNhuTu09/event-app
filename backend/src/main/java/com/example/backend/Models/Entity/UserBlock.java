package com.example.backend.Models.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * Quan hệ chặn một chiều: blocker không còn thấy nội dung của blocked.
 * (blocked vẫn thấy nội dung của blocker — đúng theo yêu cầu tối thiểu
 * của Google Play. Muốn hai chiều thì sửa điều kiện NOT EXISTS trong
 * EventMomentRepository để lọc cả hai hướng.)
 *
 * Entity được tạo ở bước này để phần lọc feed (§3) dùng được ngay;
 * các endpoint block/unblock/list sẽ bổ sung ở bước 4.
 */
@Entity
@Table(
    name = "user_blocks",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_blocks",
        columnNames = {"blocker_id", "blocked_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Người thực hiện chặn. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    /** Người bị chặn. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}