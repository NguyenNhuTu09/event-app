package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.Models.Entity.Event;
import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusAndVisibility(EventStatus status, EventVisibility visibility);
    List<Event> findByOrganizer_OrganizerId(Integer organizerId);
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);
}