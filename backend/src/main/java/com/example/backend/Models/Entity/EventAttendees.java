package com.example.backend.Models.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.RegistrationStatus;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; 

@Entity
@Table(name = "event_attendees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendees {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "event_check_in_status")
    private CheckInStatus eventCheckInStatus = CheckInStatus.NOT_CHECKED_IN;

    @Column(name = "ticket_code", unique = true, nullable = false, updatable = false)
    private String ticketCode;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDING; 

    @PrePersist
    protected void onCreate() {
        if (this.ticketCode == null) {
            this.ticketCode = "TICKET-" + UUID.randomUUID().toString();
        }
    }
}