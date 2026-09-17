package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    List<ActivityAttendees> findByActivity_ActivityId(Integer activityId);
    @Query("SELECT aa.activity.activityId FROM ActivityAttendees aa " +
       "WHERE aa.eventAttendee.event.eventId = :eventId " +
       "AND LOWER(aa.eventAttendee.user.email) = LOWER(:email) " + 
       "AND aa.status IN :statuses") 
    List<Integer> findRegisteredActivityIds(
            @Param("email") String email,   
            @Param("eventId") Long eventId,
            @Param("statuses") List<RegistrationStatus> statuses 
    );

    @Query("SELECT COUNT(aa) FROM ActivityAttendees aa " +
           "WHERE aa.activity.activityId = :activityId " +
           "AND aa.status = :status")
    long countByActivity_ActivityIdAndStatus(
            @Param("activityId") Integer activityId,
            @Param("status") RegistrationStatus status
    );

    @Query("SELECT aa.activity FROM ActivityAttendees aa " +
           "WHERE aa.eventAttendee.event.eventId = :eventId " +
           "AND aa.eventAttendee.user.email = :email " +
           "AND aa.status IN :statuses")
    List<com.example.backend.Models.Entity.Activity> findRegisteredActivitiesByEventAndUser(
            @Param("eventId") Long eventId,
            @Param("email") String email,
            @Param("statuses") List<RegistrationStatus> statuses
    );

    // =================================================================
    // Sức chứa activity
    //
    // Chỉ đếm đăng ký còn hiệu lực (PENDING / APPROVED). Sẽ thay cho
    // countByActivity_ActivityId ở hai chỗ kiểm tra maxAttendees trong
    // EventServiceImpl (registerForEvent, addActivitiesToRegistration) —
    // method cũ đếm cả CANCELLED / REJECTED nên chỗ trống không bao giờ
    // được giải phóng. Method cũ giữ lại tới khi đổi xong service và test.
    // =================================================================

    @Query("""
            SELECT COUNT(aa) FROM ActivityAttendees aa
             WHERE aa.activity.activityId = :activityId
               AND aa.status IN (com.example.backend.Utils.RegistrationStatus.PENDING,
                                 com.example.backend.Utils.RegistrationStatus.APPROVED)
            """)
    long countOccupiedSeats(@Param("activityId") Integer activityId);

    // =================================================================
    // Xoá tài khoản
    //
    // Huỷ đăng ký activity thuộc các vé của user ở sự kiện CHƯA KẾT THÚC.
    // Cùng điều kiện với EventAttendeesRepository.cancelUnfinishedRegistrationsOfUser.
    //
    // Subquery KHÔNG lọc theo ea.status, nên gọi trước hay sau method bên
    // EventAttendeesRepository đều cho cùng kết quả.
    // :now PHẢI là AppTime.now().
    // =================================================================

    @Modifying
    @Query("""
            UPDATE ActivityAttendees aa
               SET aa.status = com.example.backend.Utils.RegistrationStatus.CANCELLED
             WHERE aa.status IN (com.example.backend.Utils.RegistrationStatus.PENDING,
                                 com.example.backend.Utils.RegistrationStatus.APPROVED)
               AND aa.eventAttendee.id IN (
                     SELECT ea.id FROM EventAttendees ea
                      WHERE ea.user.id = :userId
                        AND ea.event.endDate > :now)
            """)
    int cancelUnfinishedActivityRegistrationsOfUser(@Param("userId") Long userId,
                                                    @Param("now") LocalDateTime now);
}