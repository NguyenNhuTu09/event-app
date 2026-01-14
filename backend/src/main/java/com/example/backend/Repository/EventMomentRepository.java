package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.EventMoment;

public interface EventMomentRepository extends JpaRepository<EventMoment, Long> {
    @Query("SELECT m FROM EventMoment m JOIN FETCH m.user WHERE m.event.id = :eventId ORDER BY m.postedAt DESC")
    List<EventMoment> findByEventIdWithUser(@Param("eventId") Long eventId);
    
    @Query("SELECT m FROM EventMoment m WHERE m.event.endDate < :thresholdDate")
    List<EventMoment> findExpiredMoments(@Param("thresholdDate") LocalDateTime thresholdDate);

    @Modifying
    @Query("DELETE FROM EventMoment m WHERE m.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);


    @Query(value = "SELECT m FROM EventMoment m JOIN FETCH m.user WHERE m.event.id = :eventId",
           countQuery = "SELECT COUNT(m) FROM EventMoment m WHERE m.event.id = :eventId")
    Page<EventMoment> findByEventIdWithUser(@Param("eventId") Long eventId, Pageable pageable);

    Optional<EventMoment> findByIdAndUser_Id(Long id, Long userId);
    
    
    @Query("SELECT m FROM EventMoment m JOIN FETCH m.user WHERE m.event.id = :eventId AND m.user.id = :userId ORDER BY m.postedAt DESC")
    List<EventMoment> findByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
