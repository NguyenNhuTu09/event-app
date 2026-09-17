package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.User;
import com.example.backend.Utils.RegistrationStatus;

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

    long countByEvent_EventId(Long eventId);

    // =================================================================
    // Xoá tài khoản
    //
    // Huỷ các vé PENDING / APPROVED của user cho sự kiện CHƯA KẾT THÚC.
    // Vé của sự kiện đã kết thúc được giữ nguyên làm lịch sử (user đã ẩn danh).
    //
    // - :now PHẢI là AppTime.now(): endDate là giờ VN do organizer nhập.
    // - Sự kiện đang diễn ra cũng tính là chưa kết thúc: tài khoản đã xoá
    //   thì vé không còn giá trị, kể cả khi đã check-in cổng.
    // - Vé CANCELLED tự động bị organizerCheckInUser từ chối (chỉ nhận APPROVED)
    //   và bị EventReminderScheduler bỏ qua (chỉ lấy APPROVED).
    // - Bulk update bỏ qua persistence context: gọi xong đừng dùng lại các
    //   EventAttendees đã nạp trong cùng transaction.
    // =================================================================

    @Modifying
    @Query("""
            UPDATE EventAttendees ea
               SET ea.status = com.example.backend.Utils.RegistrationStatus.CANCELLED
             WHERE ea.user.id = :userId
               AND ea.status IN (com.example.backend.Utils.RegistrationStatus.PENDING,
                                 com.example.backend.Utils.RegistrationStatus.APPROVED)
               AND ea.event.eventId IN (
                     SELECT e.eventId FROM Event e
                      WHERE e.endDate > :now)
            """)
    int cancelUnfinishedRegistrationsOfUser(@Param("userId") Long userId,
                                            @Param("now") LocalDateTime now);
}