package com.example.backend.Models.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.Utils.EditRequestStatus;
import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizers organizer;

    @NotBlank(message = "Tên sự kiện không được để trống")
    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Thời gian bắt đầu là bắt buộc")
    // @Future(message = "Thời gian bắt đầu phải ở tương lai")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "Thời gian kết thúc là bắt buộc")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @NotBlank(message = "Địa điểm không được để trống")
    private String location;

    @Column(name = "banner_image_url")
    private String bannerImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventVisibility visibility = EventVisibility.PUBLIC;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "event_qr_code", unique = true, nullable = false, updatable = false)
    private String eventQrCode;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured = false; 

    @Column(name = "is_upcoming", nullable = false)
    private boolean isUpcoming = false; 

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_edit_locked", nullable = false)
    private boolean isEditLocked = false; 

    @Enumerated(EnumType.STRING)
    @Column(name = "edit_request_status")
    private EditRequestStatus editRequestStatus = EditRequestStatus.NONE;

    @Column(name = "edit_request_reason", columnDefinition = "TEXT")
    private String editRequestReason;

    @PrePersist
    protected void onCreate() {
        if (this.eventQrCode == null) {
            this.eventQrCode = "EVT-" + UUID.randomUUID().toString();
        }
    }
}