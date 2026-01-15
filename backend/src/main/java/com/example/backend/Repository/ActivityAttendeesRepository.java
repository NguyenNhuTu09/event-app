package com.example.backend.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.ActivityAttendees;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Utils.RegistrationStatus;

@Repository
public interface ActivityAttendeesRepository extends JpaRepository<ActivityAttendees, Long> {
    long countByActivity_ActivityId(Integer activityId);
    List<ActivityAttendees> findByEventAttendee(EventAttendees eventAttendee);
    Optional<ActivityAttendees> findByActivity_ActivityIdAndEventAttendee_User_Id(Integer activityId, Long userId);
    boolean existsByEventAttendee_IdAndActivity_ActivityId(Long eventAttendeeId, Integer activityId);

    @Query("SELECT aa.activity.activityId FROM ActivityAttendees aa " +
       "WHERE aa.eventAttendee.event.eventId = :eventId " +
       "AND LOWER(aa.eventAttendee.user.email) = LOWER(:email) " + 
       "AND aa.status IN :statuses") 
    List<Integer> findRegisteredActivityIds(
            @Param("email") String email,   
            @Param("eventId") Long eventId,
            @Param("statuses") List<RegistrationStatus> statuses 
    );
}