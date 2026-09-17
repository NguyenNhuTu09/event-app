package com.example.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.example.backend.Service.Listener.AccountDeletedEvent;
import com.example.backend.Service.ServiceImpl.AccountDeletionServiceImpl;
import com.example.backend.Utils.AuthProvider;
import com.example.backend.Utils.Role;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountDeletionServiceImpl – Unit Tests")
class AccountDeletionServiceImplTest {

    @Mock private UserRepository               userRepository;
    @Mock private EventRepository              eventRepository;
    @Mock private OrganizersRepository         organizersRepository;
    @Mock private EventAttendeesRepository     eventAttendeesRepository;
    @Mock private ActivityAttendeesRepository  activityAttendeesRepository;
    @Mock private EventMomentRepository        momentRepository;
    @Mock private MomentReportRepository       reportRepository;
    @Mock private UserBlockRepository          userBlockRepository;
    @Mock private FavoritePresenterRepository  favoritePresenterRepository;
    @Mock private AccountEmailService          accountEmailService;
    @Mock private ApplicationEventPublisher    eventPublisher;

    // Encoder thật (cost thấp cho nhanh) để kiểm tra OTP được hash đúng cách.
    @Spy  private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @InjectMocks
    private AccountDeletionServiceImpl service;

    private static final String EMAIL = "user@test.com";
    private static final String UID   = "3f2a1b4c-5d6e-7f80-9a1b-2c3d4e5f6a7b";

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "otpTtlMinutes", 10L);
        ReflectionTestUtils.setField(service, "otpCooldownSeconds", 60L);
        ReflectionTestUtils.setField(service, "otpMaxAttempts", 5);

        user = new User();
        user.setId(42L);
        user.setUid(UID);
        user.setEmail(EMAIL);
        user.setUsername("nguyenvana");
        user.setPassword("$2a$hash");
        user.setRole(Role.USER);
        user.setProvider(AuthProvider.LOCAL);
        user.setPhoneNumber("0900000000");
        user.setAvatarUrl("https://res.cloudinary.com/demo/image/upload/v1/event_app/uploads/avatar.jpg");
        user.setRefreshToken("refresh");
        user.setSubscribedNews(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        org.springframework.security.core.userdetails.User
                                .withUsername(EMAIL).password("x").authorities("USER").build(),
                        null));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void currentUserIs(User u) {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(u));
    }

    private void giveValidOtp(String otp) {
        user.setDeletionOtpHash(passwordEncoder.encode(otp));
        user.setDeletionOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setDeletionOtpAttempts(0);
    }

    private static MomentCleanupView moment(Long id, Long eventId, String imageUrl) {
        return new MomentCleanupView() {
            public Long getId() { return id; }
            public Long getEventId() { return eventId; }
            public String getImageUrl() { return imageUrl; }
        };
    }

    // =========================================================================

    @Nested
    @DisplayName("Điều kiện chặn xoá")
    class Guards {

        @Test
        @DisplayName("SADMIN không được xin OTP xoá -> 403")
        void sadminForbidden() {
            user.setRole(Role.SADMIN);
            currentUserIs(user);

            assertThatThrownBy(() -> service.requestDeletionOtp())
                    .isInstanceOf(ForbiddenException.class);
            verify(accountEmailService, never()).sendDeletionOtpEmail(anyString(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("Còn sự kiện đang hoạt động -> 409, message nêu tên sự kiện")
        void organizerWithActiveEventsBlocked() {
            currentUserIs(user);
            when(eventRepository.findActiveEventNamesOwnedByUser(eq(42L), any()))
                    .thenReturn(List.of("Hội thảo AI", "Workshop UX"));

            assertThatThrownBy(() -> service.requestDeletionOtp())
                    .isInstanceOf(AccountDeletionBlockedException.class)
                    .hasMessageContaining("Hội thảo AI")
                    .hasMessageContaining("Workshop UX");
        }

        @Test
        @DisplayName("Bị chặn thì không mất lượt nhập OTP")
        void blockedDoesNotConsumeOtpAttempt() {
            giveValidOtp("123456");
            currentUserIs(user);
            when(eventRepository.findActiveEventNamesOwnedByUser(eq(42L), any()))
                    .thenReturn(List.of("Hội thảo AI"));

            assertThatThrownBy(() -> service.deleteCurrentAccount("000000", null))
                    .isInstanceOf(AccountDeletionBlockedException.class);
            assertThat(user.getDeletionOtpAttempts()).isZero();
        }

        @Test
        @DisplayName("Admin xoá tài khoản đã xoá -> 404")
        void adminDeleteAlreadyDeleted() {
            User admin = new User();
            admin.setId(1L);
            admin.setRole(Role.SADMIN);
            currentUserIs(admin);

            user.setDeletedAt(LocalDateTime.now().minusDays(1));
            when(userRepository.findByUid(UID)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.deleteAccountByAdmin(UID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================

    @Nested
    @DisplayName("Xin OTP")
    class RequestOtp {

        @Test
        @DisplayName("Lưu hash (không lưu OTP thô), gửi mail đúng mã, trả thời hạn cho client")
        void storesHashAndEmailsOtp() {
            currentUserIs(user);

            DeletionOtpResponseDTO response = service.requestDeletionOtp();

            assertThat(response.getExpiresInSeconds()).isEqualTo(600);
            assertThat(response.getResendAfterSeconds()).isEqualTo(60);

            ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
            verify(accountEmailService).sendDeletionOtpEmail(eq(EMAIL), eq("nguyenvana"), otpCaptor.capture(), eq(10L));

            String sentOtp = otpCaptor.getValue();
            assertThat(sentOtp).matches("\\d{6}");
            assertThat(user.getDeletionOtpHash()).isNotEqualTo(sentOtp);
            assertThat(passwordEncoder.matches(sentOtp, user.getDeletionOtpHash())).isTrue();
            assertThat(user.getDeletionOtpExpiry()).isAfter(LocalDateTime.now().plusMinutes(9));
            assertThat(user.getDeletionOtpAttempts()).isZero();
        }

        @Test
        @DisplayName("Xin lại trong 60 giây -> 429")
        void cooldown() {
            // Mã vừa cấp 10 giây trước: hạn = lúc cấp + 10 phút
            user.setDeletionOtpExpiry(LocalDateTime.now().minusSeconds(10).plusMinutes(10));
            currentUserIs(user);

            assertThatThrownBy(() -> service.requestDeletionOtp())
                    .isInstanceOf(TooManyRequestsException.class);
        }

        @Test
        @DisplayName("Mã đã bị huỷ vì nhập sai nhiều lần vẫn phải chờ hết cooldown")
        void cooldownSurvivesInvalidation() {
            user.setDeletionOtpHash(null); // đã bị huỷ
            user.setDeletionOtpExpiry(LocalDateTime.now().minusSeconds(5).plusMinutes(10));
            currentUserIs(user);

            assertThatThrownBy(() -> service.requestDeletionOtp())
                    .isInstanceOf(TooManyRequestsException.class);
        }
    }

    // =========================================================================

    @Nested
    @DisplayName("Xác thực OTP khi xoá")
    class VerifyOtp {

        @Test
        @DisplayName("Chưa xin mã -> 400")
        void noOtpIssued() {
            currentUserIs(user);

            assertThatThrownBy(() -> service.deleteCurrentAccount("123456", null))
                    .isInstanceOf(InvalidOtpException.class);
        }

        @Test
        @DisplayName("Mã hết hạn -> 400 và mã bị huỷ")
        void expired() {
            giveValidOtp("123456");
            user.setDeletionOtpExpiry(LocalDateTime.now().minusSeconds(1));
            currentUserIs(user);

            assertThatThrownBy(() -> service.deleteCurrentAccount("123456", null))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("hết hạn");
            assertThat(user.getDeletionOtpHash()).isNull();
        }

        @Test
        @DisplayName("Sai mã -> 400, tăng số lần sai, tài khoản chưa bị xoá")
        void wrongOtpIncrementsAttempts() {
            giveValidOtp("123456");
            currentUserIs(user);

            assertThatThrownBy(() -> service.deleteCurrentAccount("654321", null))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("còn 4 lần");

            assertThat(user.getDeletionOtpAttempts()).isEqualTo(1);
            assertThat(user.getDeletedAt()).isNull();
            // any(Object.class): ép chọn overload publishEvent(Object) — any() trần sẽ khớp nhầm
            // overload publishEvent(ApplicationEvent) và test luôn qua.
            verify(eventPublisher, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Sai lần thứ 5 -> 429, mã bị huỷ nhưng vẫn giữ mốc cooldown")
        void fifthWrongOtpInvalidates() {
            giveValidOtp("123456");
            user.setDeletionOtpAttempts(4);
            LocalDateTime expiry = user.getDeletionOtpExpiry();
            currentUserIs(user);

            assertThatThrownBy(() -> service.deleteCurrentAccount("654321", null))
                    .isInstanceOf(TooManyRequestsException.class);

            assertThat(user.getDeletionOtpHash()).isNull();
            assertThat(user.getDeletionOtpExpiry()).isEqualTo(expiry);
        }
    }

    // =========================================================================

    @Nested
    @DisplayName("Xoá thành công")
    class Deletion {

        private static final String REPORTED_URL =
                "https://res.cloudinary.com/demo/image/upload/v1/event_app/uploads/reported.jpg";
        private static final String NORMAL_URL =
                "https://res.cloudinary.com/demo/image/upload/v1/event_app/uploads/normal.jpg";

        @Test
        @DisplayName("Ẩn danh hoá toàn bộ dữ liệu cá nhân")
        void anonymizesUser() {
            giveValidOtp("123456");
            currentUserIs(user);

            service.deleteCurrentAccount("123456", "Không dùng nữa");

            String token = UID.replace("-", "");
            assertThat(user.getEmail()).isEqualTo("deleted_" + token + "@deleted.invalid");
            assertThat(user.getUsername()).isEqualTo("deleted_" + token);
            assertThat(user.getPassword()).isNull();
            assertThat(user.isEnabled()).isFalse();
            assertThat(user.getPhoneNumber()).isNull();
            assertThat(user.getAvatarUrl()).isNull();
            assertThat(user.getRefreshToken()).isNull();
            assertThat(user.isSubscribedNews()).isFalse();
            assertThat(user.getDeletionOtpHash()).isNull();
            assertThat(user.getDeletedAt()).isNotNull();

            // Giữ lại
            assertThat(user.getUid()).isEqualTo(UID);
            assertThat(user.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Dọn dữ liệu liên quan và phát event kèm email gốc, loại ảnh bằng chứng")
        void cleansRelatedDataAndPublishesEvent() {
            giveValidOtp("123456");
            currentUserIs(user);

            when(momentRepository.findCleanupViewsByUserId(42L)).thenReturn(List.of(
                    moment(7L, 100L, REPORTED_URL),
                    moment(8L, 101L, NORMAL_URL)));
            when(reportRepository.findEvidenceImageUrlsByOwner(42L)).thenReturn(List.of(REPORTED_URL));

            Organizers organizer = new Organizers();
            organizer.setContactEmail("contact@org.vn");
            organizer.setContactPhoneNumber("0911111111");
            organizer.setName("CLB Công nghệ");
            organizer.setSlug("clb-cong-nghe");
            when(organizersRepository.findByUser_Id(42L)).thenReturn(List.of(organizer));

            service.deleteCurrentAccount("123456", null);

            verify(activityAttendeesRepository).cancelUnfinishedActivityRegistrationsOfUser(eq(42L), any());
            verify(eventAttendeesRepository).cancelUnfinishedRegistrationsOfUser(eq(42L), any());
            verify(reportRepository).detachFromMoments(List.of(7L, 8L));
            verify(momentRepository).deleteByIds(List.of(7L, 8L));
            verify(userBlockRepository).deleteAllInvolvingUser(42L);
            verify(favoritePresenterRepository).deleteAllByUserId(42L);

            assertThat(organizer.getContactEmail()).isNull();
            assertThat(organizer.getContactPhoneNumber()).isNull();
            assertThat(organizer.isLocked()).isTrue();
            assertThat(organizer.getName()).isEqualTo("CLB Công nghệ");
            assertThat(organizer.getSlug()).isEqualTo("clb-cong-nghe");

            ArgumentCaptor<AccountDeletedEvent> captor = ArgumentCaptor.forClass(AccountDeletedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            AccountDeletedEvent event = captor.getValue();

            assertThat(event.originalEmail()).isEqualTo(EMAIL);
            assertThat(event.originalUsername()).isEqualTo("nguyenvana");
            assertThat(event.deletedByAdmin()).isFalse();
            assertThat(event.deletedMoments()).containsExactly(
                    new AccountDeletedEvent.MomentRef(7L, 100L),
                    new AccountDeletedEvent.MomentRef(8L, 101L));
            assertThat(event.mediaUrlCandidates())
                    .contains(NORMAL_URL, "https://res.cloudinary.com/demo/image/upload/v1/event_app/uploads/avatar.jpg")
                    .doesNotContain(REPORTED_URL);
        }

        @Test
        @DisplayName("Không có moment thì không gọi detach/delete với danh sách rỗng")
        void noMomentsSkipsBulkDelete() {
            giveValidOtp("123456");
            currentUserIs(user);

            service.deleteCurrentAccount("123456", null);

            verify(reportRepository, never()).detachFromMoments(any());
            verify(momentRepository, never()).deleteByIds(any());
        }
    }
}