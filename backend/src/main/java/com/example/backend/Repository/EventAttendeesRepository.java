package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.User;
import com.example.backend.Utils.RegistrationStatus;

@Repository
public interface EventAttendeesRepository extends JpaRepository<EventAttendees, Long> {
    
    boolean existsByEventAndUser(Event event, User user);

    List<EventAttendees> findByEvent_EventId(Long eventId);
    
    List<EventAttendees> findByUser_Id(Long userId);

    Optional<EventAttendees> findByTicketCode(String ticketCode);

    List<EventAttendees> findByUser_IdOrderByRegistrationDateDesc(Long userId);

    Optional<EventAttendees> findByEvent_EventIdAndUser_Id(Long eventId, Long userId);

    Optional<EventAttendees> findByEventAndUser(Event event, User user);

    @Query("SELECT ea FROM EventAttendees ea " +
           "WHERE ea.status = :status " +
           "AND ea.event.status = com.example.backend.Utils.EventStatus.PUBLISHED " +
           "AND ea.event.startDate BETWEEN :start AND :end")
    List<EventAttendees> findAllApprovedAttendeesForDateRange(
            @Param("status") RegistrationStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}