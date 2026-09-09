package com.example.backend.Models.Entity;

import java.time.LocalDateTime;

import com.example.backend.Utils.MomentStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_moments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMoment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    /**
     * Trạng thái kiểm duyệt.
     *
     * @Builder.Default là BẮT BUỘC: entity này đang được khởi tạo bằng
     * EventMoment.builder() trong createMoment(). Nếu thiếu annotation này,
     * Lombok sẽ bỏ qua giá trị khởi tạo và mọi bài đăng mới sẽ có
     * status = null -> lọt qua điều kiện lọc feed và biến mất khỏi danh sách.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MomentStatus status = MomentStatus.VISIBLE;

    @PrePersist
    protected void onCreate() {
        this.postedAt = LocalDateTime.now();
        // Lưới an toàn thứ hai, phòng trường hợp entity được tạo bằng
        // new EventMoment() rồi set field thủ công ở chỗ khác.
        if (this.status == null) {
            this.status = MomentStatus.VISIBLE;
        }
    }
}