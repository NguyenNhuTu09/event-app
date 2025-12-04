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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "activity")
@NoArgsConstructor
@ToString(exclude = {"event", "category", "presenter"})
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Integer activityId;

    // --- RELATIONSHIPS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Liên kết với ActivityCategories
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private ActivityCategories category;

    // Liên kết với Presenters (đã tạo ở bước trước)
    // Có thể null (nullable = true) vì không phải hoạt động nào cũng có diễn giả
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presenter_id")
    private Presenters presenter;

    // --- BASIC FIELDS ---

    @NotBlank(message = "Tên hoạt động không được để trống")
    @Size(max = 255)
    @Column(name = "activity_name", nullable = false, length = 255)
    private String activityName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Thời gian bắt đầu là bắt buộc")
    @Column(name = "start_time")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc là bắt buộc")
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "max_attendees")
    private Long maxAttendees;

    // Lưu trữ JSON dưới dạng String. 
    // Ví dụ: "['ROLE_VIP', 'ROLE_MEMBER']"
    @Column(name = "accessible_to", columnDefinition = "json")
    private String accessibleTo;

    @Size(max = 255)
    @Column(name = "room_or_venue", length = 255)
    private String roomOrVenue;

    @Size(max = 255)
    @Column(name = "materials_url", length = 255)
    private String materialsUrl;
}