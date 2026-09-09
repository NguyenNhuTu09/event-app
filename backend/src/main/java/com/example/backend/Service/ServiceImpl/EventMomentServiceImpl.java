package com.example.backend.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Request.MomentReportRequestDTO;
import com.example.backend.DTO.Request.MomentRequestDTO;
import com.example.backend.DTO.Response.MomentReportResponseDTO;
import com.example.backend.DTO.Response.MomentResponseDTO;
import com.example.backend.DTO.Response.SocketResponseDTO;
import com.example.backend.Exception.DuplicateReportException;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.EventMoment;
import com.example.backend.Models.Entity.MomentReport;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Repository.EventMomentRepository;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.MomentReportRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.ModerationEmailService;
import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.MomentStatus;
import com.example.backend.Utils.ReportReason;
import com.example.backend.Utils.ReportStatus;
import com.example.backend.Utils.Role;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventMomentServiceImpl {

    private final EventMomentRepository momentRepository;
    private final EventAttendeesRepository eventAttendeesRepository;
    private final EventRepository eventRepository;
    private final MomentReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModerationEmailService moderationEmailService;

    private static final int DAYS_TO_KEEP_MOMENTS = 3;

    /** Số người báo cáo khác nhau đủ để tự động ẩn bài chờ admin xem xét. */
    private static final int AUTO_HIDE_THRESHOLD = 3;

    private static final java.time.format.DateTimeFormatter DATE_FMT =
            java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    // =================================================================
    // ĐĂNG BÀI
    // =================================================================

    @Transactional
    public MomentResponseDTO createMoment(Long eventId, MomentRequestDTO request, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (LocalDateTime.now().isAfter(event.getEndDate().plusDays(DAYS_TO_KEEP_MOMENTS))) {
            throw new IllegalArgumentException("Sự kiện đã kết thúc quá 3 ngày, tính năng này đã đóng.");
        }

        EventAttendees attendee = eventAttendeesRepository.findByEvent_EventIdAndUser_Id(eventId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa đăng ký tham gia sự kiện này."));

        if (attendee.getEventCheckInStatus() != CheckInStatus.CHECKED_IN) {
            throw new IllegalArgumentException("Bạn cần check-in tại sự kiện để sử dụng tính năng này.");
        }

        User user = attendee.getUser();

        // Chặn người đang bị admin tạm khoá quyền đăng nội dung (§5).
        if (user.isMomentSuspended()) {
            throw new IllegalArgumentException(
                    "Tài khoản của bạn đang bị tạm khoá quyền đăng nội dung đến "
                    + user.getMomentSuspendedUntil().format(DATE_FMT) + ".");
        }

        EventMoment moment = EventMoment.builder()
                .event(event)
                .user(user)
                .caption(request.getCaption())
                .imageUrl(request.getImageUrl())
                .status(MomentStatus.VISIBLE)
                .build();

        EventMoment saved = momentRepository.save(moment);
        MomentResponseDTO dto = toDTO(saved);

        // Chỉ phát realtime cho bài đang hiển thị.
        if (saved.getStatus() == MomentStatus.VISIBLE) {
            publishMomentEvent(eventId, "CREATE", dto);
        }
        return dto;
    }

    // =================================================================
    // ĐỌC FEED (đã lọc kiểm duyệt)
    // =================================================================

    public Page<MomentResponseDTO> getEventMoments(Long eventId, Long viewerId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (LocalDateTime.now().isAfter(event.getEndDate().plusDays(DAYS_TO_KEEP_MOMENTS))) {
            return Page.empty(pageable);
        }

        return momentRepository.findVisibleForViewer(eventId, viewerId, pageable)
                .map(this::toDTO);
    }

    public List<MomentResponseDTO> getEventMoments(Long eventId, Long viewerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (LocalDateTime.now().isAfter(event.getEndDate().plusDays(DAYS_TO_KEEP_MOMENTS))) {
            return List.of();
        }

        return momentRepository.findVisibleForViewer(eventId, viewerId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<MomentResponseDTO> getMyMoments(Long eventId, Long userId) {
        return momentRepository.findMyMoments(eventId, userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // =================================================================
    // SỬA / XOÁ BÀI CỦA CHÍNH MÌNH
    // =================================================================

    @Transactional
    public MomentResponseDTO updateMoment(Long eventId, Long momentId, MomentRequestDTO request, Long userId) {
        EventMoment moment = momentRepository.findByIdAndUser_Id(momentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết hoặc bạn không có quyền sửa."));

        if (moment.getStatus() != MomentStatus.VISIBLE) {
            throw new IllegalArgumentException("Bài viết đang bị kiểm duyệt, không thể chỉnh sửa.");
        }

        moment.setCaption(request.getCaption());
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            moment.setImageUrl(request.getImageUrl());
        }

        EventMoment updated = momentRepository.save(moment);
        MomentResponseDTO dto = toDTO(updated);
        publishMomentEvent(eventId, "UPDATE", dto);
        return dto;
    }

    @Transactional
    public void deleteMoment(Long eventId, Long momentId, Long userId) {
        EventMoment moment = momentRepository.findByIdAndUser_Id(momentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết hoặc bạn không có quyền xóa."));

        // Gỡ liên kết trước khi xoá cứng, nếu không sẽ vi phạm khoá ngoại.
        // Bản ghi báo cáo vẫn giữ nguyên (kèm snapshot) để admin còn hồ sơ đối chiếu.
        reportRepository.detachFromMoment(momentId);

        momentRepository.delete(moment);
        publishMomentEvent(eventId, "DELETE", momentId);
    }

    // =================================================================
    // BÁO CÁO NỘI DUNG (§1)
    // =================================================================

    @Transactional
    public MomentReportResponseDTO reportMoment(Long eventId,
                                                Long momentId,
                                                MomentReportRequestDTO request,
                                                Long reporterId) {

        EventMoment moment = momentRepository.findByIdAndEventId(momentId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết trong sự kiện này."));

        if (moment.getUser().getId().equals(reporterId)) {
            throw new IllegalArgumentException("Bạn không thể báo cáo bài viết của chính mình.");
        }

        if (reportRepository.existsByMoment_IdAndReporter_Id(momentId, reporterId)) {
            throw new DuplicateReportException("Bạn đã báo cáo bài viết này rồi.");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MomentReport report = MomentReport.builder()
                .moment(moment)
                .reporter(reporter)
                .reason(request.getReason())
                .detail(request.getDetail())
                .status(ReportStatus.PENDING)
                .eventId(eventId)
                .momentOwnerId(moment.getUser().getId())
                .momentImageUrl(moment.getImageUrl())
                .momentCaption(moment.getCaption())
                .build();

        MomentReport saved = reportRepository.save(report);

        // Log phục vụ khiếu nại / đối chiếu với Google.
        log.info("MODERATION report#{} momentId={} eventId={} reporterId={} ownerId={} reason={}",
                saved.getId(), momentId, eventId, reporterId, moment.getUser().getId(), request.getReason());

        applyAutoHideIfNeeded(moment, eventId, request.getReason(), reporterId);

        return MomentReportResponseDTO.builder()
                .id(saved.getId())
                .status(saved.getStatus())
                .build();
    }

    /**
     * Tự động ẩn bài khi:
     *   - lý do là CSAE (chỉ cần 1 báo cáo), hoặc
     *   - có từ AUTO_HIDE_THRESHOLD người khác nhau báo cáo.
     *
     * Báo cáo đã bị admin bác (DISMISSED) không được tính, tránh việc một
     * bài đã xác minh là hợp lệ bị dìm lại bằng những báo cáo cũ.
     */
    private void applyAutoHideIfNeeded(EventMoment moment, Long eventId,
                                        ReportReason reason, Long reporterId) {
        boolean isCsae = reason == ReportReason.CSAE;

        // Cảnh báo CSAE gửi kể cả khi bài đã bị ẩn từ trước: mỗi báo cáo
        // loại này đều phải đến tay người xử lý, không được nuốt im lặng.
        if (isCsae) {
            notifyAdminsOfCsae(moment, eventId, reporterId);
        }

        if (moment.getStatus() != MomentStatus.VISIBLE) {
            return; // đã bị ẩn/gỡ trước đó rồi
        }

        // countBy chạy sau save() trong cùng transaction: Hibernate tự flush
        // trước khi thực thi query nên báo cáo vừa tạo đã được tính vào.
        long activeReports = reportRepository.countByMoment_IdAndStatusNot(
                moment.getId(), ReportStatus.DISMISSED);

        if (!isCsae && activeReports < AUTO_HIDE_THRESHOLD) {
            return;
        }

        moment.setStatus(MomentStatus.UNDER_REVIEW);
        momentRepository.save(moment);

        log.warn("MODERATION auto-hide momentId={} eventId={} reports={} csae={}",
                moment.getId(), eventId, activeReports, isCsae);

        // Đẩy DELETE để mọi client đang mở feed gỡ bài xuống ngay lập tức.
        // Chủ bài viết sẽ thấy lại bài kèm nhãn "đang kiểm duyệt" sau khi refresh.
        publishMomentEvent(eventId, "DELETE", moment.getId());
    }

    /**
     * Báo cáo CSAE phải có đường xử lý riêng theo yêu cầu của Google, không
     * nằm chung hàng đợi báo cáo thường. Ở đây gửi mail khẩn cho toàn bộ SADMIN.
     */
    private void notifyAdminsOfCsae(EventMoment moment, Long eventId, Long reporterId) {
        log.error("MODERATION CSAE report on momentId={} eventId={} — cần xử lý khẩn cấp",
                moment.getId(), eventId);
        try {
            List<String> adminEmails = userRepository.findByRole(Role.SADMIN)
                    .stream().map(User::getEmail).collect(Collectors.toList());

            moderationEmailService.sendCsaeAlert(
                    adminEmails, moment.getId(), eventId,
                    reporterId, moment.getUser().getId(), moment.getImageUrl());
        } catch (Exception e) {
            log.error("Không gửi được cảnh báo CSAE momentId={}: {}", moment.getId(), e.getMessage());
        }
    }

    // =================================================================
    // JOB DỌN DẸP
    // =================================================================

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredMoments() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(DAYS_TO_KEEP_MOMENTS);

        List<EventMoment> expiredMoments = momentRepository.findExpiredMoments(threshold);
        if (expiredMoments.isEmpty()) {
            return;
        }

        List<Long> ids = expiredMoments.stream().map(EventMoment::getId).collect(Collectors.toList());

        // BẮT BUỘC gọi trước deleteByIds: moment_reports đang giữ khoá ngoại
        // tới event_moments. Bỏ bước này thì job sẽ ném lỗi ràng buộc và
        // không xoá được gì cả.
        int detached = reportRepository.detachFromMoments(ids);

        momentRepository.deleteByIds(ids);
        log.info("Đã xoá {} khoảnh khắc hết hạn, gỡ liên kết {} báo cáo.", ids.size(), detached);
    }

    // =================================================================
    // Tiện ích dùng chung (ModerationAdminServiceImpl gọi vào đây)
    // =================================================================

    /** Phát một sự kiện lên topic realtime của sự kiện. */
    public void publishMomentEvent(Long eventId, String type, Object data) {
        SocketResponseDTO payload = SocketResponseDTO.builder()
                .type(type)
                .data(data)
                .build();
        messagingTemplate.convertAndSend("/topic/event/" + eventId + "/moments", payload);
    }

    public MomentResponseDTO toDTO(EventMoment entity) {
        if (entity == null) {
            return null;
        }
        Long userId = entity.getUser() != null ? entity.getUser().getId() : null;
        String username = entity.getUser() != null ? entity.getUser().getUsername() : "Unknown";
        String avatarUrl = entity.getUser() != null ? entity.getUser().getAvatarUrl() : null;

        return MomentResponseDTO.builder()
                .id(entity.getId())
                .userId(userId)
                .username(username)
                .userAvatar(avatarUrl)
                .caption(entity.getCaption())
                .imageUrl(entity.getImageUrl())
                .postedAt(entity.getPostedAt())
                .timeAgo(calculateTimeAgo(entity.getPostedAt()))
                .status(entity.getStatus() == null ? MomentStatus.VISIBLE : entity.getStatus())
                .build();
    }

    private String calculateTimeAgo(LocalDateTime postedAt) {
        if (postedAt == null) {
            return "";
        }

        long seconds = java.time.Duration.between(postedAt, LocalDateTime.now()).getSeconds();

        if (seconds < 60) {
            return "Vừa xong";
        } else if (seconds < 3600) {
            return (seconds / 60) + " phút trước";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " giờ trước";
        } else if (seconds < 259200) {
            return (seconds / 86400) + " ngày trước";
        } else {
            return postedAt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }
}