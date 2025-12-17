package com.example.backend.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.ActivityAttendees;
import com.example.backend.Models.Entity.EventAttendees;

@Repository
public interface ActivityAttendeesRepository extends JpaRepository<ActivityAttendees, Long> {
    long countByActivity_ActivityId(Integer activityId);
    List<ActivityAttendees> findByEventAttendee(EventAttendees eventAttendee);
}