package com.example.backend.Models.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_attendees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityAttendees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết đến vé tham dự sự kiện tổng (Cha)
    // Từ đây có thể suy ra User là ai
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_attendee_id", nullable = false)
    private EventAttendees eventAttendee;

    // Liên kết đến hoạt động cụ thể mà họ đăng ký
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt = LocalDateTime.now();

    // Trạng thái điểm danh riêng cho hoạt động này (VD: Có mặt tại phòng hội thảo A)
    @Column(name = "check_in_status")
    private boolean checkInStatus = false;
    
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;
}