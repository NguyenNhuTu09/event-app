package com.example.backend.Service.ServiceImpl;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.DTO.Response.DeletionOtpResponseDTO;
import com.example.backend.Exception.AccountDeletionBlockedException;
import com.example.backend.Exception.ForbiddenException;
import com.example.backend.Exception.InvalidOtpException;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Exception.TooManyRequestsException;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.ActivityAttendeesRepository;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Repository.EventMomentRepository;
import com.example.backend.Repository.EventMomentRepository.MomentCleanupView;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.FavoritePresenterRepository;
import com.example.backend.Repository.MomentReportRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.UserBlockRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.AccountEmailService;
import com.example.backend.Service.Interface.AccountDeletionService;
import com.example.backend.Service.Listener.AccountDeletedEvent;
import com.example.backend.Utils.AppTime;
import com.example.backend.Utils.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GHI CHÚ VỀ MÚI GIỜ (xem AppTime)
 * ================================
 *   - So với event.endDate (giờ VN do organizer nhập)       -> AppTime.now()
 *   - deletedAt, deletionOtpExpiry (server sinh, giờ UTC)    -> LocalDateTime.now()
 *
 * GHI CHÚ VỀ TRANSACTION
 * ======================
 * Dùng @Transactional của Spring (không phải jakarta) vì cần noRollbackFor:
 * số lần nhập sai OTP phải được lưu dù request ném lỗi.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountDeletionServiceImpl implements AccountDeletionService {

    private static final SecureRandom OTP_RANDOM = new SecureRandom();
    private static final int MAX_REASON_LOG_LENGTH = 500;

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final OrganizersRepository organizersRepository;
    private final EventAttendeesRepository eventAttendeesRepository;
    private final ActivityAttendeesRepository activityAttendeesRepository;
    private final EventMomentRepository momentRepository;
    private final MomentReportRepository reportRepository;
    private final UserBlockRepository userBlockRepository;
    private final FavoritePresenterRepository favoritePresenterRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountEmailService accountEmailService;
    private final ApplicationEventPublisher eventPublisher;

    /** Hạn dùng của OTP. */
    @Value("${app.account-deletion.otp-ttl-minutes:10}")
    private long otpTtlMinutes;

    /** Thời gian tối thiểu giữa hai lần xin OTP. */
    @Value("${app.account-deletion.otp-cooldown-seconds:60}")
    private long otpCooldownSeconds;

    /** Nhập sai tới lần thứ này thì mã bị huỷ. */
    @Value("${app.account-deletion.otp-max-attempts:5}")
    private int otpMaxAttempts;

    // =================================================================
    // 1. XIN MÃ OTP
    // =================================================================

    @Override
    @Transactional
    public DeletionOtpResponseDTO requestDeletionOtp() {
        User me = getCurrentUser();

        // Guard chạy ngay từ bước xin mã: organizer còn sự kiện / SADMIN biết
        // là không xoá được trước khi phải mở email lấy mã.
        assertDeletable(me);

        LocalDateTime now = LocalDateTime.now();
        long waitSeconds = secondsUntilNextOtpAllowed(me, now);
        if (waitSeconds > 0) {
            throw new TooManyRequestsException(
                    "Vui lòng đợi " + waitSeconds + " giây trước khi yêu cầu mã mới.");
        }

        String otp = String.format("%06d", OTP_RANDOM.nextInt(1_000_000));
        me.setDeletionOtpHash(passwordEncoder.encode(otp));
        me.setDeletionOtpExpiry(now.plusMinutes(otpTtlMinutes));
        me.setDeletionOtpAttempts(0);
        userRepository.save(me);

        accountEmailService.sendDeletionOtpEmail(me.getEmail(), me.getUsername(), otp, otpTtlMinutes);
        log.info("ACCOUNT_DELETION otp-issued userId={}", me.getId());

        return DeletionOtpResponseDTO.builder()
                .message("Mã xác nhận đã được gửi tới email của bạn.")
                .expiresInSeconds(otpTtlMinutes * 60)
                .resendAfterSeconds(otpCooldownSeconds)
                .build();
    }

    // =================================================================
    // 2. NGƯỜI DÙNG TỰ XOÁ
    // =================================================================

    @Override
    @Transactional(noRollbackFor = { InvalidOtpException.class, TooManyRequestsException.class })
    public void deleteCurrentAccount(String otp, String reason) {
        User me = getCurrentUser();

        // Guard trước OTP: người bị chặn không mất lượt nhập mã vô ích.
        assertDeletable(me);
        verifyOtp(me, otp);

        performDeletion(me, false, reason);
    }

    // =================================================================
    // 3. SADMIN XOÁ HỘ
    // =================================================================

    @Override
    @Transactional
    public void deleteAccountByAdmin(String uid) {
        User admin = getCurrentUser();

        User target = userRepository.findByUid(uid)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với UID: " + uid));

        assertDeletable(target);

        log.warn("ACCOUNT_DELETION admin-initiated adminId={} targetUserId={}", admin.getId(), target.getId());
        performDeletion(target, true, null);
    }

    // =================================================================
    // Điều kiện được phép xoá
    // =================================================================

    private void assertDeletable(User user) {
        if (user.isDeleted()) {
            throw new ResourceNotFoundException("Tài khoản không tồn tại hoặc đã bị xoá.");
        }

        // SADMIN không xoá qua luồng này, kể cả SADMIN xoá SADMIN khác:
        // tránh hệ thống rơi vào tình trạng không còn quản trị viên.
        if (user.getRole() == Role.SADMIN) {
            throw new ForbiddenException("Không thể xoá tài khoản quản trị viên hệ thống.");
        }

        // AppTime: so với event.endDate là giờ VN
        List<String> activeEvents = eventRepository.findActiveEventNamesOwnedByUser(user.getId(), AppTime.now());
        if (!activeEvents.isEmpty()) {
            throw new AccountDeletionBlockedException(activeEvents);
        }
    }

    // =================================================================
    // OTP
    // =================================================================

    private void verifyOtp(User user, String otp) {
        if (user.getDeletionOtpHash() == null || user.getDeletionOtpExpiry() == null) {
            throw new InvalidOtpException(
                    "Bạn chưa yêu cầu mã xác nhận, hoặc mã đã bị huỷ. Vui lòng yêu cầu mã mới.");
        }

        if (user.getDeletionOtpExpiry().isBefore(LocalDateTime.now())) {
            invalidateOtp(user);
            throw new InvalidOtpException("Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới.");
        }

        String candidate = (otp == null) ? "" : otp.trim();
        boolean correct = candidate.matches("\\d{6}")
                && passwordEncoder.matches(candidate, user.getDeletionOtpHash());

        if (correct) {
            return;
        }

        int attempts = user.getDeletionOtpAttempts() + 1;
        if (attempts >= otpMaxAttempts) {
            invalidateOtp(user);
            log.warn("ACCOUNT_DELETION otp-locked userId={} attempts={}", user.getId(), attempts);
            throw new TooManyRequestsException(
                    "Bạn đã nhập sai mã " + attempts + " lần. Mã đã bị huỷ, vui lòng yêu cầu mã mới.");
        }

        user.setDeletionOtpAttempts(attempts);
        userRepository.save(user);
        throw new InvalidOtpException(
                "Mã xác nhận không chính xác. Bạn còn " + (otpMaxAttempts - attempts) + " lần thử.");
    }

    /**
     * Huỷ mã hiện tại nhưng GIỮ deletionOtpExpiry: cooldown được suy ra từ cột
     * này, xoá nó đi thì người dò mã có thể xin mã mới ngay lập tức.
     */
    private void invalidateOtp(User user) {
        user.setDeletionOtpHash(null);
        user.setDeletionOtpAttempts(0);
        userRepository.save(user);
    }

    /**
     * Không lưu thời điểm cấp mã; suy ra từ hạn dùng. Nếu đổi otp-ttl-minutes
     * giữa hai lần xin mã thì chỉ cooldown của đúng lần đó bị lệch, và luôn bị
     * kẹp trong khoảng [0, otpCooldownSeconds].
     */
    private long secondsUntilNextOtpAllowed(User user, LocalDateTime now) {
        if (user.getDeletionOtpExpiry() == null) {
            return 0;
        }
        LocalDateTime issuedAt = user.getDeletionOtpExpiry().minusMinutes(otpTtlMinutes);
        long elapsed = Duration.between(issuedAt, now).getSeconds();
        return Math.min(otpCooldownSeconds, Math.max(0, otpCooldownSeconds - elapsed));
    }

    // =================================================================
    // Xoá
    // =================================================================

    private void performDeletion(User user, boolean deletedByAdmin, String reason) {
        Long userId = user.getId();

        // Chụp lại trước khi ẩn danh hoá: listener cần để gửi email xác nhận.
        String originalEmail = user.getEmail();
        String originalUsername = user.getUsername();
        String originalAvatarUrl = user.getAvatarUrl();

        // AppTime: mốc "sự kiện chưa kết thúc" so với event.endDate (giờ VN)
        LocalDateTime vnNow = AppTime.now();

        // --- 1. Vé & đăng ký activity của sự kiện chưa kết thúc -----------
        // Hai câu độc lập với nhau (xem comment trong repository).
        int cancelledActivities = activityAttendeesRepository
                .cancelUnfinishedActivityRegistrationsOfUser(userId, vnNow);
        int cancelledRegistrations = eventAttendeesRepository
                .cancelUnfinishedRegistrationsOfUser(userId, vnNow);

        // --- 2. Khoảnh khắc -------------------------------------------------
        List<MomentCleanupView> moments = momentRepository.findCleanupViewsByUserId(userId);
        List<Long> momentIds = moments.stream().map(MomentCleanupView::getId).toList();

        if (!momentIds.isEmpty()) {
            // BẮT BUỘC trước deleteByIds: moment_reports giữ khoá ngoại tới
            // event_moments. Báo cáo vẫn còn nguyên kèm snapshot.
            reportRepository.detachFromMoments(momentIds);
            momentRepository.deleteByIds(momentIds);
        }

        // Ứng viên xoá file = ảnh moment + avatar, trừ ảnh đang là bằng chứng
        // trong hồ sơ báo cáo. Listener kiểm tra thêm sau commit.
        Set<String> mediaCandidates = new LinkedHashSet<>();
        moments.stream()
                .map(MomentCleanupView::getImageUrl)
                .filter(Objects::nonNull)
                .forEach(mediaCandidates::add);
        if (originalAvatarUrl != null) {
            mediaCandidates.add(originalAvatarUrl);
        }
        int candidatesBefore = mediaCandidates.size();
        mediaCandidates.removeAll(new HashSet<>(reportRepository.findEvidenceImageUrlsByOwner(userId)));
        int evidenceKept = candidatesBefore - mediaCandidates.size();

        // --- 3. Quan hệ chặn & diễn giả yêu thích ---------------------------
        int blocksRemoved = userBlockRepository.deleteAllInvolvingUser(userId);
        int favoritesRemoved = favoritePresenterRepository.deleteAllByUserId(userId);

        // --- 4. Hồ sơ organizer ---------------------------------------------
        // Giữ name + slug để sự kiện cũ vẫn hiện đúng nhà tổ chức; xoá thông
        // tin liên hệ và khoá lại. KHÔNG xoá dòng organizers: events.organizer_id
        // có ON DELETE CASCADE.
        List<Organizers> organizers = organizersRepository.findByUser_Id(userId);
        for (Organizers organizer : organizers) {
            organizer.setContactEmail(null);
            organizer.setContactPhoneNumber(null);
            organizer.setLocked(true);
            organizer.setUnlockRequested(false);
            organizer.setUnlockRequestReason(null);
        }
        if (!organizers.isEmpty()) {
            organizersRepository.saveAll(organizers);
        }

        // --- 5. Ẩn danh hoá user ----------------------------------------------
        anonymize(user);
        userRepository.save(user);

        // --- 6. Việc ngoài DB: chạy sau commit ------------------------------
        List<AccountDeletedEvent.MomentRef> momentRefs = moments.stream()
                .map(m -> new AccountDeletedEvent.MomentRef(m.getId(), m.getEventId()))
                .toList();

        eventPublisher.publishEvent(new AccountDeletedEvent(
                userId,
                originalEmail,
                originalUsername,
                deletedByAdmin,
                momentRefs,
                List.copyOf(mediaCandidates)));

        log.warn("ACCOUNT_DELETION done userId={} byAdmin={} cancelledRegistrations={} cancelledActivities={} "
                        + "moments={} mediaCandidates={} evidenceKept={} blocksRemoved={} favoritesRemoved={} "
                        + "organizersLocked={} reason={}",
                userId, deletedByAdmin, cancelledRegistrations, cancelledActivities,
                momentIds.size(), mediaCandidates.size(), evidenceKept, blocksRemoved, favoritesRemoved,
                organizers.size(), sanitizeForLog(reason));
    }

    /**
     * Xoá dữ liệu cá nhân nhưng giữ dòng users.
     *
     * Giữ lại: id, uid, role, provider (không phải dữ liệu cá nhân, cần cho
     * thống kê); momentSuspendedUntil và contentPolicyAccepted* (giá trị audit).
     */
    private void anonymize(User user) {
        // uid là UUID duy nhất và không đổi (updatable = false): ghép vào email
        // và username thì không bao giờ đụng ràng buộc UNIQUE.
        // Tên miền .invalid được dành riêng (RFC 2606), không nhận được mail.
        // Đổi email cũng làm mọi JWT cũ mất hiệu lực vì subject của token là email.
        String token = user.getUid().replace("-", "");
        user.setEmail("deleted_" + token + "@deleted.invalid");
        user.setUsername("deleted_" + token);

        user.setPassword(null);
        user.setEnabled(false);

        user.setAddress(null);
        user.setGender(null);
        user.setDateOfBirth(null);
        user.setPhoneNumber(null);
        user.setAvatarUrl(null);
        user.setAvatarS3Key(null);

        user.setRefreshToken(null);
        user.setRefreshTokenExpiryDate(null);
        user.setResetPasswordToken(null);
        user.setTokenExpiryDate(null);
        user.setVerificationCode(null);
        user.setSubscribedNews(false);

        user.setDeletionOtpHash(null);
        user.setDeletionOtpExpiry(null);
        user.setDeletionOtpAttempts(0);

        // Server sinh -> giờ UTC, cùng hệ với các mốc server sinh khác.
        user.setDeletedAt(LocalDateTime.now());
    }

    private String sanitizeForLog(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String oneLine = reason.replaceAll("[\\r\\n\\t]+", " ").trim();
        return oneLine.length() > MAX_REASON_LOG_LENGTH
                ? oneLine.substring(0, MAX_REASON_LOG_LENGTH) + "…"
                : oneLine;
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails ud) ? ud.getUsername() : principal.toString();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại."));
    }
}