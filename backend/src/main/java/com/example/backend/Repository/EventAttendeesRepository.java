package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.User;

@Repository
public interface EventAttendeesRepository extends JpaRepository<EventAttendees, Long> {
    
    boolean existsByEventAndUser(Event event, User user);

    List<EventAttendees> findByEvent_EventId(Long eventId);
    
    List<EventAttendees> findByUser_Id(Long userId);

    Optional<EventAttendees> findByTicketCode(String ticketCode);

    List<EventAttendees> findByUser_IdOrderByRegistrationDateDesc(Long userId);
}