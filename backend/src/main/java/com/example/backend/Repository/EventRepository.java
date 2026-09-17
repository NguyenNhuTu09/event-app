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
       "AND e.createdAt >= :sevenDaysAgo " + 
       "AND e.endDate > CURRENT_TIMESTAMP " +
       "AND (e.registrationDeadline IS NULL OR e.registrationDeadline > CURRENT_TIMESTAMP)")
    List<Event> findNewAndOpenEvents(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    @Query("SELECT e FROM Event e WHERE e.status = :status ORDER BY e.createdAt DESC")
    List<Event> findByStatusOrderByCreatedAtDesc(@Param("status") EventStatus status);
    
    @Query("SELECT e FROM Event e WHERE e.editRequestStatus = :editStatus ORDER BY e.createdAt DESC")
    List<Event> findByEditRequestStatus(@Param("editStatus") com.example.backend.Utils.EditRequestStatus editStatus);

    /**
     * Xoá tài khoản — điều kiện chặn organizer.
     *
     * Tên các sự kiện CHƯA KẾT THÚC ở trạng thái PENDING_APPROVAL / PUBLISHED /
     * IN_PROGRESS thuộc bất kỳ organizer nào của user. Danh sách rỗng = được
     * phép xoá. Trả tên thay vì boolean để thông báo 409 nói rõ sự kiện nào
     * đang chặn.
     *
     * - :now PHẢI là AppTime.now(): endDate là giờ VN.
     * - Lọc endDate để sự kiện có status bị "kẹt" (ví dụ vẫn IN_PROGRESS dù đã
     *   qua ngày) không chặn oan người dùng.
     * - Sự kiện DRAFT / REJECTED / CANCELLED / COMPLETED không chặn.
     */
    @Query("""
            SELECT e.eventName FROM Event e
             WHERE e.organizer.user.id = :userId
               AND e.status IN (com.example.backend.Utils.EventStatus.PENDING_APPROVAL,
                                com.example.backend.Utils.EventStatus.PUBLISHED,
                                com.example.backend.Utils.EventStatus.IN_PROGRESS)
               AND e.endDate > :now
             ORDER BY e.startDate ASC
            """)
    List<String> findActiveEventNamesOwnedByUser(@Param("userId") Long userId,
                                                 @Param("now") LocalDateTime now);
}