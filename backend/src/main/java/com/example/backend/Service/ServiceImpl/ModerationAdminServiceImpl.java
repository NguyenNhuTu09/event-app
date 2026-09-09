package com.example.backend.Service.ServiceImpl;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.backend.DTO.Response.AdminReportResponseDTO;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.EventMoment;
import com.example.backend.Models.Entity.MomentReport;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.EventMomentRepository;
import com.example.backend.Repository.MomentReportRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.Interface.ModerationAdminService;
import com.example.backend.Service.ModerationEmailService;
import com.example.backend.Utils.MomentStatus;
import com.example.backend.Utils.ReportStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModerationAdminServiceImpl implements ModerationAdminService {

    private final MomentReportRepository reportRepository;
    private final EventMomentRepository momentRepository;
    private final UserRepository userRepository;
    private final ModerationEmailService moderationEmailService;

    /** Dùng nhờ hai tiện ích công khai: toDTO() và publishMomentEvent(). */
    private final EventMomentServiceImpl momentService;

    // =================================================================
    // DANH SÁCH BÁO CÁO
    // =================================================================

    @Override
    public Page<AdminReportResponseDTO> getReports(ReportStatus status, Pageable pageable) {
        Page<MomentReport> reports = (status == null)
                ? reportRepository.findAllForAdmin(pageable)
                : reportRepository.findByStatusForAdmin(status, pageable);

        return reports.map(this::mapToAdminDTO);
    }

    private AdminReportResponseDTO mapToAdminDTO(MomentReport r) {
        EventMoment moment = r.getMoment();
        boolean deleted = (moment == null);

        // Ưu tiên dữ liệu sống; nếu moment đã bị xoá thì dùng bản snapshot
        // lưu trong chính bản ghi báo cáo.
        String imageUrl = deleted ? r.getMomentImageUrl() : moment.getImageUrl();
        String caption = deleted ? r.getMomentCaption() : moment.getCaption();
        Long ownerId = deleted ? r.getMomentOwnerId() : moment.getUser().getId();

        String ownerUsername = null;
        String ownerEmail = null;
        LocalDateTime ownerSuspendedUntil = null;

        if (!deleted) {
            ownerUsername = moment.getUser().getUsername();
            ownerEmail = moment.getUser().getEmail();
            ownerSuspendedUntil = moment.getUser().getMomentSuspendedUntil();
        } else if (ownerId != null) {
            // Một query phụ cho mỗi dòng. Chấp nhận được vì trang admin nhỏ
            // và chỉ xảy ra với các báo cáo có moment đã bị xoá.
            User owner = userRepository.findById(ownerId).orElse(null);
            if (owner != null) {
                ownerUsername = owner.getUsername();
                ownerEmail = owner.getEmail();
                ownerSuspendedUntil = owner.getMomentSuspendedUntil();
            }
        }

        Long totalReports = deleted ? null
                : reportRepository.countByMoment_IdAndStatusNot(moment.getId(), ReportStatus.DISMISSED);

        Long hoursPending = (r.getStatus() == ReportStatus.PENDING && r.getCreatedAt() != null)
                ? Duration.between(r.getCreatedAt(), LocalDateTime.now()).toHours()
                : null;

        return AdminReportResponseDTO.builder()
                .id(r.getId())
                .reason(r.getReason())
                .detail(r.getDetail())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .resolutionNote(r.getResolutionNote())
                .resolvedByUsername(r.getResolvedBy() != null ? r.getResolvedBy().getUsername() : null)
                .hoursPending(hoursPending)
                .eventId(r.getEventId())
                .momentId(deleted ? null : moment.getId())
                .momentStatus(deleted ? null : moment.getStatus())
                .momentImageUrl(imageUrl)
                .momentCaption(caption)
                .momentDeleted(deleted)
                .totalReportsOnMoment(totalReports)
                .ownerId(ownerId)
                .ownerUsername(ownerUsername)
                .ownerEmail(ownerEmail)
                .ownerSuspendedUntil(ownerSuspendedUntil)
                .reporterId(r.getReporter().getId())
                .reporterUsername(r.getReporter().getUsername())
                .reporterEmail(r.getReporter().getEmail())
                .build();
    }

    // =================================================================
    // GỠ BÀI
    // =================================================================

    @Override
    @Transactional
    public void removeMoment(Long momentId, String note) {
        EventMoment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết."));

        if (moment.getStatus() == MomentStatus.REMOVED) {
            throw new IllegalArgumentException("Bài viết này đã bị gỡ trước đó.");
        }

        User admin = getCurrentUser();
        String finalNote = (note == null || note.isBlank())
                ? "Vi phạm Quy tắc cộng đồng."
                : note;

        moment.setStatus(MomentStatus.REMOVED);
        momentRepository.save(moment);

        int closed = reportRepository.closePendingReports(
                momentId, ReportStatus.RESOLVED, admin, LocalDateTime.now(), finalNote);

        log.warn("MODERATION remove momentId={} adminId={} closedReports={} note={}",
                momentId, admin.getId(), closed, finalNote);

        // Gỡ khỏi mọi client đang mở feed.
        momentService.publishMomentEvent(moment.getEvent().getEventId(), "DELETE", momentId);

        // Báo cho chủ bài viết — Google yêu cầu người dùng được thông báo
        // khi nội dung của họ bị gỡ, kèm lý do.
        try {
            moderationEmailService.sendMomentRemovedEmail(
                    moment.getUser().getEmail(),
                    moment.getUser().getUsername(),
                    moment.getEvent().getEventName(),
                    moment.getCaption(),
                    finalNote);
        } catch (Exception e) {
            log.error("Lỗi gửi mail gỡ bài momentId={}: {}", momentId, e.getMessage());
        }
    }

    // =================================================================
    // KHÔI PHỤC BÀI
    // =================================================================

    @Override
    @Transactional
    public void restoreMoment(Long momentId) {
        EventMoment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết."));

        if (moment.getStatus() == MomentStatus.VISIBLE) {
            throw new IllegalArgumentException("Bài viết này đang hiển thị bình thường.");
        }

        User admin = getCurrentUser();

        moment.setStatus(MomentStatus.VISIBLE);
        momentRepository.save(moment);

        // Bác các báo cáo đang chờ. Quan trọng: countByMoment_IdAndStatusNot
        // bỏ qua DISMISSED, nên bài đã được xác minh là hợp lệ sẽ không bị
        // những báo cáo cũ đẩy trở lại UNDER_REVIEW ngay lập tức.
        int closed = reportRepository.closePendingReports(
                momentId, ReportStatus.DISMISSED, admin, LocalDateTime.now(),
                "Đã xem xét, nội dung không vi phạm.");

        log.info("MODERATION restore momentId={} adminId={} dismissedReports={}",
                momentId, admin.getId(), closed);

        // Đưa bài trở lại feed của những người đang mở app.
        momentService.publishMomentEvent(
                moment.getEvent().getEventId(), "CREATE", momentService.toDTO(moment));
    }

    // =================================================================
    // TẠM KHOÁ QUYỀN ĐĂNG BÀI
    // =================================================================

    @Override
    @Transactional
    public void suspendUser(Long userId, int days, String reason) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng."));

        User admin = getCurrentUser();

        // Cộng dồn từ mốc hiện tại nếu người này đang bị khoá, để một lệnh
        // suspend mới không vô tình rút ngắn hình phạt đang có.
        LocalDateTime base = target.isMomentSuspended()
                ? target.getMomentSuspendedUntil()
                : LocalDateTime.now();

        LocalDateTime until = base.plusDays(days);
        target.setMomentSuspendedUntil(until);
        userRepository.save(target);

        String finalReason = (reason == null || reason.isBlank())
                ? "Vi phạm Quy tắc cộng đồng."
                : reason;

        log.warn("MODERATION suspend userId={} adminId={} days={} until={} reason={}",
                userId, admin.getId(), days, until, finalReason);

        try {
            moderationEmailService.sendMomentSuspendedEmail(
                    target.getEmail(), target.getUsername(), until, finalReason);
        } catch (Exception e) {
            log.error("Lỗi gửi mail suspend userId={}: {}", userId, e.getMessage());
        }
    }

    // =================================================================

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy quản trị viên hiện tại."));
    }
}