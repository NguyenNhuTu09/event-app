package com.example.backend.Models.Entity;

import java.time.LocalDateTime;

import com.example.backend.Utils.CheckInStatus;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_attendee_id", nullable = false)
    private EventAttendees eventAttendee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "check_in_status")
    private CheckInStatus actCheckInStatus = CheckInStatus.NOT_CHECKED_IN;
    
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;
}