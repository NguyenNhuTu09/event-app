package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {

    List<Activity> findByEvent_EventIdOrderByStartTimeAsc(Long eventId);

    List<Activity> findByPresenter_PresenterIdOrderByStartTimeAsc(Integer presenterId);

    List<Activity> findByEvent_EventIdAndCategory_CategoryId(Long eventId, Integer categoryId);

    Optional<Activity> findByActivityQrCode(String activityQrCode);

    @Query("SELECT a FROM Activity a WHERE a.event.eventId = :eventId AND " +
           "a.startTime >= :startOfDay AND a.startTime < :endOfDay " +
           "ORDER BY a.startTime ASC")
    List<Activity> findByEventAndDate(@Param("eventId") Long eventId,
                                      @Param("startOfDay") LocalDateTime startOfDay,
                                      @Param("endOfDay") LocalDateTime endOfDay);

  
    @Query("SELECT a FROM Activity a WHERE a.event.eventId = :eventId AND " +
           "(LOWER(a.activityName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Activity> searchActivitiesInEvent(@Param("eventId") Long eventId, 
                                           @Param("keyword") String keyword);


    @Query("SELECT COUNT(a) > 0 FROM Activity a " +
           "WHERE a.event.eventId = :eventId " +
           "AND a.roomOrVenue = :room " +
           "AND a.activityId != :currentActivityId " + 
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean existsByRoomConflict(@Param("eventId") Long eventId,
                                 @Param("room") String room,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("currentActivityId") Integer currentActivityId);


    @Query("SELECT COUNT(a) > 0 FROM Activity a " +
           "WHERE a.presenter.presenterId = :presenterId " +
           "AND a.activityId != :currentActivityId " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean existsByPresenterConflict(@Param("presenterId") Integer presenterId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("currentActivityId") Integer currentActivityId);
}