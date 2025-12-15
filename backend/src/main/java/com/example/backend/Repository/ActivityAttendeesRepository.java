package com.example.backend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.ActivityAttendees;

@Repository
public interface ActivityAttendeesRepository extends JpaRepository<ActivityAttendees, Long> {
    long countByActivity_ActivityId(Integer activityId);
}