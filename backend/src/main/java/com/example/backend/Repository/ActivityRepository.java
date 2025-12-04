package com.example.backend.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.Models.Entity.Activity;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {

    // --- CÁC TRUY VẤN CƠ BẢN ---

    // 1. Lấy toàn bộ lịch trình của một Sự kiện (Sắp xếp theo giờ bắt đầu)
    // Đây là API quan trọng nhất để hiển thị Agenda cho người dùng
    List<Activity> findByEvent_EventIdOrderByStartTimeAsc(Long eventId);

    // 2. Lấy danh sách hoạt động của một Diễn giả cụ thể
    // Giúp hiển thị profile diễn giả: "Các phiên tôi tham gia"
    List<Activity> findByPresenter_PresenterIdOrderByStartTimeAsc(Integer presenterId);

    // 3. Lọc hoạt động theo Loại trong một Sự kiện (VD: Chỉ lấy Workshop của Event A)
    List<Activity> findByEvent_EventIdAndCategory_CategoryId(Long eventId, Integer categoryId);

    // 4. Lấy hoạt động theo ngày cụ thể (Trong sự kiện kéo dài nhiều ngày)
    @Query("SELECT a FROM Activity a WHERE a.event.eventId = :eventId AND " +
           "a.startTime >= :startOfDay AND a.startTime < :endOfDay " +
           "ORDER BY a.startTime ASC")
    List<Activity> findByEventAndDate(@Param("eventId") Long eventId,
                                      @Param("startOfDay") LocalDateTime startOfDay,
                                      @Param("endOfDay") LocalDateTime endOfDay);

    // --- TÌM KIẾM ---

    // 5. Tìm kiếm hoạt động theo tên hoặc mô tả trong một sự kiện
    @Query("SELECT a FROM Activity a WHERE a.event.eventId = :eventId AND " +
           "(LOWER(a.activityName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Activity> searchActivitiesInEvent(@Param("eventId") Long eventId, 
                                           @Param("keyword") String keyword);

    // --- KIỂM TRA LOGIC (VALIDATION) ---

    // 6. Kiểm tra trùng phòng/địa điểm (Room Conflict)
    // Logic: Hoạt động mới có thời gian chồng chéo với hoạt động cũ tại cùng địa điểm không?
    // Công thức overlap: (StartA < EndB) && (EndA > StartB)
    @Query("SELECT COUNT(a) > 0 FROM Activity a " +
           "WHERE a.event.eventId = :eventId " +
           "AND a.roomOrVenue = :room " +
           "AND a.activityId != :currentActivityId " + // Loại trừ chính nó khi update
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean existsByRoomConflict(@Param("eventId") Long eventId,
                                 @Param("room") String room,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("currentActivityId") Integer currentActivityId);

    // 7. Kiểm tra trùng lịch của Diễn giả (Speaker Conflict)
    // Diễn giả không thể ở 2 nơi cùng lúc
    @Query("SELECT COUNT(a) > 0 FROM Activity a " +
           "WHERE a.presenter.presenterId = :presenterId " +
           "AND a.activityId != :currentActivityId " +
           "AND (a.startTime < :endTime AND a.endTime > :startTime)")
    boolean existsByPresenterConflict(@Param("presenterId") Integer presenterId,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime,
                                      @Param("currentActivityId") Integer currentActivityId);
}