package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.Event;
import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusAndVisibility(EventStatus status, EventVisibility visibility);
    List<Event> findByOrganizer_OrganizerId(Integer organizerId);
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Event> findByStatusNot(EventStatus status);

    List<Event> findByIsFeaturedTrueAndStatusAndVisibility(EventStatus status, EventVisibility visibility);

    List<Event> findByIsUpcomingTrueAndStatusAndVisibility(EventStatus status, EventVisibility visibility);

    List<Event> findByIsFeaturedTrue();

    List<Event> findByIsUpcomingTrue();
    
    @Query("SELECT e FROM Event e WHERE " +
       "e.status = com.example.backend.Utils.EventStatus.PUBLISHED " +
       "AND e.createdAt >= :sevenDaysAgo " + // Hoặc dùng publishedAt nếu bạn có trường đó
       "AND e.endDate > CURRENT_TIMESTAMP " +
       "AND (e.registrationDeadline IS NULL OR e.registrationDeadline > CURRENT_TIMESTAMP)")
    List<Event> findNewAndOpenEvents(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);
}