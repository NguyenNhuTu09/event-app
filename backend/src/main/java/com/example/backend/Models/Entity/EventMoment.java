package com.example.backend.Models.Entity;

import java.time.LocalDateTime;
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

    @PrePersist
    protected void onCreate() {
        this.postedAt = LocalDateTime.now();
    }
}
