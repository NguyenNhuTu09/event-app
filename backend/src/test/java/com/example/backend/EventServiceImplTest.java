package com.example.backend;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.backend.DTO.Request.EventRegistrationRequestDTO;
import com.example.backend.DTO.Request.EventRequestDTO;
import com.example.backend.DTO.Response.EventResponseDTO;
import com.example.backend.Exception.AccountLockedException;
import com.example.backend.Exception.ResourceNotFoundException;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.ActivityAttendeesRepository;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Service.EmailService;
import com.example.backend.Service.ServiceImpl.EventServiceImpl;
import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.EditRequestStatus;
import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;
import com.example.backend.Utils.RegistrationStatus;

/**
 * Unit test cho EventServiceImpl.
 *
 * Các nhóm test:
 *  1. createEvent
 *  2. updateEvent
 *  3. deleteEvent
 *  4. approveEvent / rejectEvent
 *  5. submitEventForApproval
 *  6. registerForEvent
 *  7. approveRegistration / rejectRegistration
 *  8. getFeaturedEvents / updateFeaturedEvents
 *  9. getUpcomingEvents / updateUpcomingEvents
 * 10. requestEditPermission / approveEditPermission / rejectEditPermission
 * 11. getMyRegistrationHistory / toggleNewsletterSubscription
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl – Unit Tests")
class EventServiceImplTest {

    // ────────────────────────────────────────────────────────────────────────────
    // Mocks
    // ────────────────────────────────────────────────────────────────────────────

    @Mock private EventRepository              eventRepository;
    @Mock private OrganizersRepository         organizersRepository;
    @Mock private EventAttendeesRepository     eventAttendeesRepository;
    @Mock private UserRepository               userRepository;
    @Mock private ActivityRepository           activityRepository;
    @Mock private ActivityAttendeesRepository  activityAttendeesRepository;
    @Mock private EmailService                 emailService;

    @InjectMocks
    private EventServiceImpl eventService;

    // ────────────────────────────────────────────────────────────────────────────
    // Fixtures dùng chung
    // ────────────────────────────────────────────────────────────────────────────

    private static final String ORGANIZER_EMAIL = "organizer@test.com";
    private static final String USER_EMAIL      = "user@test.com";

    private User         organizerUser;
    private User         regularUser;
    private Organizers   organizer;
    private Event        event;

    @BeforeEach
    void setUp() {
        // --- User của Organizer ---
        organizerUser = new User();
        organizerUser.setId(1L);
        organizerUser.setEmail(ORGANIZER_EMAIL);
        organizerUser.setUsername("organizer_user");

        // --- User thường ---
        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setEmail(USER_EMAIL);
        regularUser.setUsername("regular_user");

        // --- Organizer entity ---
        organizer = new Organizers();
        organizer.setOrganizerId(10);
        organizer.setName("Tổ Chức Test");
        organizer.setApproved(true);
        organizer.setLocked(false);
        organizer.setUser(organizerUser);

        // --- Event entity mẫu ---
        event = new Event();
        event.setEventId(100L);
        event.setEventName("Sự Kiện Test");
        event.setSlug("su-kien-test");
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.DRAFT);
        event.setVisibility(EventVisibility.PUBLIC);
        event.setStartDate(LocalDateTime.now().plusDays(5));
        event.setEndDate(LocalDateTime.now().plusDays(6));
        event.setLocation("Hà Nội");
        event.setEditLocked(false);
        event.setEditRequestStatus(EditRequestStatus.NONE);
        event.setFeatured(false);
        event.setUpcoming(false);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Helper – thiết lập SecurityContext
    // ────────────────────────────────────────────────────────────────────────────

    /** Đặt principal là UserDetails (email làm username). */
    private void mockSecurityContextAsOrganizer() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(ORGANIZER_EMAIL)
                .password("pass")
                .roles("ORGANIZER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    private void mockSecurityContextAsUser() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(USER_EMAIL)
                .password("pass")
                .roles("USER")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    /** Stub chung: organizersRepository trả về organizer hợp lệ. */
    private void stubGetCurrentOrganizer() {
        when(organizersRepository.findByUser_Email(ORGANIZER_EMAIL))
                .thenReturn(Optional.of(organizer));
    }

    /** Stub chung: userRepository trả về regular user. */
    private void stubGetCurrentUser() {
        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(regularUser));
    }

    /** Tạo EventRequestDTO hợp lệ (ngày trong tương lai). */
    private EventRequestDTO validEventRequest() {
        EventRequestDTO dto = new EventRequestDTO();
        dto.setEventName("Sự Kiện Mới");
        dto.setDescription("Mô tả sự kiện");
        dto.setStartDate(LocalDateTime.now().plusDays(10));
        dto.setEndDate(LocalDateTime.now().plusDays(11));
        dto.setLocation("TP.HCM");
        dto.setVisibility(EventVisibility.PUBLIC);
        return dto;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 1. createEvent
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("1. createEvent")
    class CreateEventTests {

        @Test
        @DisplayName("Tạo sự kiện thành công → trả về EventResponseDTO với status DRAFT")
        void createEvent_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            EventRequestDTO dto = validEventRequest();

            when(eventRepository.existsBySlug(anyString())).thenReturn(false);
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
                Event e = inv.getArgument(0);
                e.setEventId(200L);
                return e;
            });

            EventResponseDTO result = eventService.createEvent(dto);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(EventStatus.DRAFT);
            assertThat(result.getEventName()).isEqualTo(dto.getEventName());
            verify(eventRepository).save(any(Event.class));
        }

        @Test
        @DisplayName("Tạo sự kiện khi organizer bị khóa → ném AccountLockedException")
        void createEvent_organizerLocked_throwsException() {
            mockSecurityContextAsOrganizer();
            organizer.setLocked(true);
            stubGetCurrentOrganizer();

            assertThatThrownBy(() -> eventService.createEvent(validEventRequest()))
                    .isInstanceOf(AccountLockedException.class);
        }

        @Test
        @DisplayName("Tạo sự kiện khi organizer chưa được duyệt → ném IllegalArgumentException")
        void createEvent_organizerNotApproved_throwsException() {
            mockSecurityContextAsOrganizer();
            organizer.setApproved(false);
            stubGetCurrentOrganizer();

            assertThatThrownBy(() -> eventService.createEvent(validEventRequest()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("chưa được phê duyệt");
        }

        @Test
        @DisplayName("Tạo sự kiện với startDate trong quá khứ → ném IllegalArgumentException")
        void createEvent_startDateInPast_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            EventRequestDTO dto = validEventRequest();
            dto.setStartDate(LocalDateTime.now().minusDays(1));
            dto.setEndDate(LocalDateTime.now().plusDays(1));

            assertThatThrownBy(() -> eventService.createEvent(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tương lai");
        }

        @Test
        @DisplayName("Tạo sự kiện với startDate sau endDate → ném IllegalArgumentException")
        void createEvent_startAfterEnd_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            EventRequestDTO dto = validEventRequest();
            dto.setStartDate(LocalDateTime.now().plusDays(5));
            dto.setEndDate(LocalDateTime.now().plusDays(3));

            assertThatThrownBy(() -> eventService.createEvent(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("trước thời gian kết thúc");
        }

        @Test
        @DisplayName("Tạo sự kiện với registrationDeadline sau startDate → ném IllegalArgumentException")
        void createEvent_deadlineAfterStart_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            EventRequestDTO dto = validEventRequest();
            dto.setRegistrationDeadline(dto.getStartDate().plusDays(1)); // deadline > start

            assertThatThrownBy(() -> eventService.createEvent(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Deadline");
        }

        @Test
        @DisplayName("Tên sự kiện bị trùng slug → generateUniqueSlug thêm hậu tố số")
        void createEvent_slugCollision_generatesUniqueSlug() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            EventRequestDTO dto = validEventRequest();
            dto.setEventName("Su Kien Trung");

            // Slug gốc đã tồn tại, slug có hậu tố "-1" thì chưa
            when(eventRepository.existsBySlug("su-kien-trung")).thenReturn(true);
            when(eventRepository.existsBySlug("su-kien-trung-1")).thenReturn(false);
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            EventResponseDTO result = eventService.createEvent(dto);
            assertThat(result.getSlug()).isEqualTo("su-kien-trung-1");
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 2. updateEvent
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("2. updateEvent")
    class UpdateEventTests {

        @Test
        @DisplayName("Cập nhật sự kiện DRAFT thành công")
        void updateEvent_draftEvent_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));
            when(eventRepository.existsBySlug(anyString())).thenReturn(false);
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            EventRequestDTO dto = validEventRequest();
            EventResponseDTO result = eventService.updateEvent("su-kien-test", dto);

            assertThat(result).isNotNull();
            assertThat(result.getEventName()).isEqualTo(dto.getEventName());
        }

        @Test
        @DisplayName("Cập nhật sự kiện không tồn tại → ném ResourceNotFoundException")
        void updateEvent_notFound_throwsException() {
            mockSecurityContextAsOrganizer();
            when(eventRepository.findBySlug("khong-ton-tai")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.updateEvent("khong-ton-tai", validEventRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Cập nhật sự kiện của organizer khác → ném RuntimeException")
        void updateEvent_notOwner_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            Organizers otherOrganizer = new Organizers();
            otherOrganizer.setOrganizerId(99);
            otherOrganizer.setApproved(true);
            otherOrganizer.setLocked(false);
            event.setOrganizer(otherOrganizer);

            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.updateEvent("su-kien-test", validEventRequest()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("không có quyền");
        }

        @Test
        @DisplayName("Cập nhật sự kiện PUBLISHED + isEditLocked → ném IllegalStateException")
        void updateEvent_publishedAndLocked_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStatus(EventStatus.PUBLISHED);
            event.setEditLocked(true);
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.updateEvent("su-kien-test", validEventRequest()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("khóa chỉnh sửa");
        }

        @Test
        @DisplayName("Cập nhật sự kiện PUBLISHED (không bị lock) → status chuyển sang PENDING_APPROVAL")
        void updateEvent_published_becomesPendingApproval() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStatus(EventStatus.PUBLISHED);
            event.setEditLocked(false);
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));
            when(eventRepository.existsBySlug(anyString())).thenReturn(false);
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            EventResponseDTO result = eventService.updateEvent("su-kien-test", validEventRequest());
            assertThat(result.getStatus()).isEqualTo(EventStatus.PENDING_APPROVAL);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 3. deleteEvent
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("3. deleteEvent")
    class DeleteEventTests {

        @Test
        @DisplayName("Xóa sự kiện DRAFT không có người đăng ký → thành công")
        void deleteEvent_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));
            when(eventAttendeesRepository.countByEvent_EventId(event.getEventId())).thenReturn(0L);

            assertThatCode(() -> eventService.deleteEvent("su-kien-test"))
                    .doesNotThrowAnyException();
            verify(eventRepository).delete(event);
        }

        @Test
        @DisplayName("Xóa sự kiện đã có người đăng ký → ném IllegalStateException")
        void deleteEvent_hasRegistrations_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));
            when(eventAttendeesRepository.countByEvent_EventId(event.getEventId())).thenReturn(5L);

            assertThatThrownBy(() -> eventService.deleteEvent("su-kien-test"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("người đăng ký");
        }

        @Test
        @DisplayName("Xóa sự kiện PUBLISHED + bị khóa → ném IllegalStateException")
        void deleteEvent_publishedAndLocked_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStatus(EventStatus.PUBLISHED);
            event.setEditLocked(true);
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.deleteEvent("su-kien-test"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Xóa sự kiện không tồn tại → ném ResourceNotFoundException")
        void deleteEvent_notFound_throwsException() {
            mockSecurityContextAsOrganizer();
            when(eventRepository.findBySlug("abc")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.deleteEvent("abc"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Xóa sự kiện khi organizer bị khóa → ném AccountLockedException")
        void deleteEvent_organizerLocked_throwsException() {
            mockSecurityContextAsOrganizer();
            organizer.setLocked(true);
            stubGetCurrentOrganizer();
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.deleteEvent("su-kien-test"))
                    .isInstanceOf(AccountLockedException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 4. approveEvent / rejectEvent  (Admin)
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("4. approveEvent / rejectEvent")
    class ApproveRejectEventTests {

        @Test
        @DisplayName("Duyệt sự kiện PENDING_APPROVAL → status PUBLISHED, editLocked = true")
        void approveEvent_success() {
            event.setStatus(EventStatus.PENDING_APPROVAL);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService)
                    .sendEventApprovedEmail(anyString(), anyString(), anyString(), any(), anyString());

            EventResponseDTO result = eventService.approveEvent(100L);

            assertThat(result.getStatus()).isEqualTo(EventStatus.PUBLISHED);
            assertThat(result.isEditLocked()).isTrue();
        }

        @Test
        @DisplayName("Duyệt sự kiện đã PUBLISHED → ném IllegalArgumentException")
        void approveEvent_alreadyPublished_throwsException() {
            event.setStatus(EventStatus.PUBLISHED);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.approveEvent(100L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("đã được công bố");
        }

        @Test
        @DisplayName("Từ chối sự kiện PENDING_APPROVAL → status REJECTED")
        void rejectEvent_success() {
            event.setStatus(EventStatus.PENDING_APPROVAL);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService)
                    .sendEventRejectedEmail(anyString(), anyString(), anyString(), anyString());

            EventResponseDTO result = eventService.rejectEvent(100L, "Nội dung vi phạm");

            assertThat(result.getStatus()).isEqualTo(EventStatus.REJECTED);
        }

        @Test
        @DisplayName("Từ chối sự kiện đã PUBLISHED → ném IllegalArgumentException")
        void rejectEvent_alreadyPublished_throwsException() {
            event.setStatus(EventStatus.PUBLISHED);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.rejectEvent(100L, "reason"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Không thể từ chối");
        }

        @Test
        @DisplayName("Duyệt/từ chối sự kiện không tồn tại → ném ResourceNotFoundException")
        void approveOrReject_notFound_throwsException() {
            when(eventRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.approveEvent(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
            assertThatThrownBy(() -> eventService.rejectEvent(999L, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 5. submitEventForApproval
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("5. submitEventForApproval")
    class SubmitForApprovalTests {

        @Test
        @DisplayName("Submit sự kiện DRAFT → status PENDING_APPROVAL")
        void submit_draftEvent_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStatus(EventStatus.DRAFT);
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService)
                    .sendEventSubmissionPending(anyString(), anyString(), anyString(), any());

            EventResponseDTO result = eventService.submitEventForApproval("su-kien-test");
            assertThat(result.getStatus()).isEqualTo(EventStatus.PENDING_APPROVAL);
        }

        @Test
        @DisplayName("Submit sự kiện REJECTED → status PENDING_APPROVAL")
        void submit_rejectedEvent_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStatus(EventStatus.REJECTED);
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService)
                    .sendEventSubmissionPending(anyString(), anyString(), anyString(), any());

            EventResponseDTO result = eventService.submitEventForApproval("su-kien-test");
            assertThat(result.getStatus()).isEqualTo(EventStatus.PENDING_APPROVAL);
        }

        @Test
        @DisplayName("Submit sự kiện đang PENDING_APPROVAL → ném IllegalArgumentException")
        void submit_alreadyPending_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStatus(EventStatus.PENDING_APPROVAL);
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.submitEventForApproval("su-kien-test"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("đang chờ duyệt");
        }

        @Test
        @DisplayName("Submit sự kiện khi organizer bị khóa → ném AccountLockedException")
        void submit_organizerLocked_throwsException() {
            mockSecurityContextAsOrganizer();
            organizer.setLocked(true);
            stubGetCurrentOrganizer();
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.submitEventForApproval("su-kien-test"))
                    .isInstanceOf(AccountLockedException.class);
        }

        @Test
        @DisplayName("Submit sự kiện của organizer khác → ném RuntimeException")
        void submit_notOwner_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            Organizers other = new Organizers();
            other.setOrganizerId(99);
            other.setApproved(true);
            other.setLocked(false);
            event.setOrganizer(other);
            event.setStatus(EventStatus.DRAFT);
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.submitEventForApproval("su-kien-test"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("không có quyền");
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 6. registerForEvent
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("6. registerForEvent")
    class RegisterForEventTests {

        private EventRegistrationRequestDTO registrationDTO() {
            EventRegistrationRequestDTO dto = new EventRegistrationRequestDTO();
            dto.setEventId(100L);
            dto.setActivityIds(Collections.emptyList());
            return dto;
        }

        @BeforeEach
        void setUpPublishedEvent() {
            event.setStatus(EventStatus.PUBLISHED);
            event.setEndDate(LocalDateTime.now().plusDays(10));
        }

        @Test
        @DisplayName("Đăng ký sự kiện thành công (không có activity)")
        void register_success_noActivities() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
            when(eventAttendeesRepository.existsByEventAndUser(event, regularUser)).thenReturn(false);
            when(eventAttendeesRepository.save(any(EventAttendees.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> eventService.registerForEvent(registrationDTO()))
                    .doesNotThrowAnyException();
            verify(eventAttendeesRepository).save(any(EventAttendees.class));
        }

        @Test
        @DisplayName("Đăng ký sự kiện chưa được công bố → ném IllegalArgumentException")
        void register_eventNotPublished_throwsException() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            event.setStatus(EventStatus.DRAFT);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.registerForEvent(registrationDTO()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("chưa được công bố");
        }

        @Test
        @DisplayName("Đăng ký sự kiện đã kết thúc → ném IllegalArgumentException")
        void register_eventEnded_throwsException() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            event.setEndDate(LocalDateTime.now().minusDays(1)); // đã kết thúc
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.registerForEvent(registrationDTO()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("kết thúc");
        }

        @Test
        @DisplayName("Đăng ký sau deadline → ném IllegalArgumentException")
        void register_afterDeadline_throwsException() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            event.setRegistrationDeadline(LocalDateTime.now().minusHours(1)); // deadline đã qua
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.registerForEvent(registrationDTO()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("hết thời hạn");
        }

        @Test
        @DisplayName("Đăng ký sự kiện đã đăng ký trước đó → ném IllegalArgumentException")
        void register_alreadyRegistered_throwsException() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
            when(eventAttendeesRepository.existsByEventAndUser(event, regularUser)).thenReturn(true);

            assertThatThrownBy(() -> eventService.registerForEvent(registrationDTO()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("đã đăng ký");
        }

        @Test
        @DisplayName("Đăng ký sự kiện không tồn tại → ném ResourceNotFoundException")
        void register_eventNotFound_throwsException() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            when(eventRepository.findById(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.registerForEvent(registrationDTO()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 7. approveRegistration / rejectRegistration
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("7. approveRegistration / rejectRegistration")
    class ApproveRejectRegistrationTests {

        private EventAttendees buildPendingRegistration() {
            EventAttendees reg = new EventAttendees();
            reg.setId(1L);
            reg.setUser(regularUser);
            reg.setEvent(event);
            reg.setStatus(RegistrationStatus.PENDING);
            reg.setEventCheckInStatus(CheckInStatus.NOT_CHECKED_IN);
            reg.setTicketCode("TICKET-ABCD");
            return reg;
        }

        @Test
        @DisplayName("Duyệt đăng ký PENDING → status APPROVED, gửi email")
        void approveRegistration_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            EventAttendees reg = buildPendingRegistration();
            when(eventAttendeesRepository.findById(1L)).thenReturn(Optional.of(reg));
            when(activityAttendeesRepository.findByEventAttendee(reg)).thenReturn(Collections.emptyList());
            when(eventAttendeesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService).sendRegistrationApprovedEmail(
                    anyString(), anyString(), anyString(), any(), any(), anyString(), anyString(), any());

            assertThatCode(() -> eventService.approveRegistration(1L))
                    .doesNotThrowAnyException();
            assertThat(reg.getStatus()).isEqualTo(RegistrationStatus.APPROVED);
        }

        @Test
        @DisplayName("Duyệt đăng ký khi organizer bị khóa → ném AccountLockedException")
        void approveRegistration_organizerLocked_throwsException() {
            mockSecurityContextAsOrganizer();
            organizer.setLocked(true);
            stubGetCurrentOrganizer();

            EventAttendees reg = buildPendingRegistration();
            when(eventAttendeesRepository.findById(1L)).thenReturn(Optional.of(reg));

            assertThatThrownBy(() -> eventService.approveRegistration(1L))
                    .isInstanceOf(AccountLockedException.class);
        }

        @Test
        @DisplayName("Duyệt đăng ký của sự kiện thuộc organizer khác → ném RuntimeException")
        void approveRegistration_notOwner_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            Organizers other = new Organizers();
            other.setOrganizerId(99);
            other.setApproved(true);
            other.setLocked(false);
            event.setOrganizer(other);
            EventAttendees reg = buildPendingRegistration();
            when(eventAttendeesRepository.findById(1L)).thenReturn(Optional.of(reg));

            assertThatThrownBy(() -> eventService.approveRegistration(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("không có quyền");
        }

        @Test
        @DisplayName("Từ chối đăng ký → status REJECTED, gửi email")
        void rejectRegistration_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            EventAttendees reg = buildPendingRegistration();
            when(eventAttendeesRepository.findById(1L)).thenReturn(Optional.of(reg));
            when(eventAttendeesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService).sendRegistrationRejectedEmail(
                    anyString(), anyString(), anyString(), any(), anyString(), anyString());

            assertThatCode(() -> eventService.rejectRegistration(1L, "Không đủ điều kiện"))
                    .doesNotThrowAnyException();
            assertThat(reg.getStatus()).isEqualTo(RegistrationStatus.REJECTED);
            verify(emailService).sendRegistrationRejectedEmail(
                    anyString(), anyString(), anyString(), any(), anyString(), anyString());
        }

        @Test
        @DisplayName("Từ chối đăng ký không tồn tại → ném ResourceNotFoundException")
        void rejectRegistration_notFound_throwsException() {
            mockSecurityContextAsOrganizer();
            when(eventAttendeesRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> eventService.rejectRegistration(999L, "reason"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 8. getFeaturedEvents / updateFeaturedEvents
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("8. getFeaturedEvents / updateFeaturedEvents")
    class FeaturedEventsTests {

        @Test
        @DisplayName("getFeaturedEvents → chỉ trả về sự kiện chưa kết thúc")
        void getFeaturedEvents_filtersEndedEvents() {
            Event active = buildPublishedEvent(1L, "active", LocalDateTime.now().plusDays(2));
            Event ended  = buildPublishedEvent(2L, "ended",  LocalDateTime.now().minusDays(1));
            active.setFeatured(true);
            ended.setFeatured(true);

            when(eventRepository.findByIsFeaturedTrueAndStatusAndVisibility(
                    EventStatus.PUBLISHED, EventVisibility.PUBLIC))
                    .thenReturn(List.of(active, ended));

            List<EventResponseDTO> result = eventService.getFeaturedEvents();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEventId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("updateFeaturedEvents với > 4 sự kiện → ném IllegalArgumentException")
        void updateFeaturedEvents_tooMany_throwsException() {
            assertThatThrownBy(() -> eventService.updateFeaturedEvents(List.of(1L, 2L, 3L, 4L, 5L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tối đa 4");
        }

        @Test
        @DisplayName("updateFeaturedEvents với sự kiện chưa PUBLISHED → ném IllegalArgumentException")
        void updateFeaturedEvents_notPublished_throwsException() {
            Event draft = buildPublishedEvent(1L, "draft-slug", LocalDateTime.now().plusDays(3));
            draft.setStatus(EventStatus.DRAFT);

            when(eventRepository.findByIsFeaturedTrue()).thenReturn(Collections.emptyList());
            when(eventRepository.findAllById(List.of(1L))).thenReturn(List.of(draft));

            assertThatThrownBy(() -> eventService.updateFeaturedEvents(List.of(1L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("chưa được công bố");
        }

        @Test
        @DisplayName("updateFeaturedEvents với sự kiện đã kết thúc → ném IllegalArgumentException")
        void updateFeaturedEvents_endedEvent_throwsException() {
            Event ended = buildPublishedEvent(1L, "ended-slug", LocalDateTime.now().minusDays(1));

            when(eventRepository.findByIsFeaturedTrue()).thenReturn(Collections.emptyList());
            when(eventRepository.findAllById(List.of(1L))).thenReturn(List.of(ended));

            assertThatThrownBy(() -> eventService.updateFeaturedEvents(List.of(1L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("đã kết thúc");
        }

        @Test
        @DisplayName("updateFeaturedEvents thành công → trả về danh sách mới")
        void updateFeaturedEvents_success() {
            Event e1 = buildPublishedEvent(1L, "slug-1", LocalDateTime.now().plusDays(5));
            Event e2 = buildPublishedEvent(2L, "slug-2", LocalDateTime.now().plusDays(5));

            when(eventRepository.findByIsFeaturedTrue()).thenReturn(Collections.emptyList());
            when(eventRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(e1, e2));
            when(eventRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            List<EventResponseDTO> result = eventService.updateFeaturedEvents(List.of(1L, 2L));
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(EventResponseDTO::isFeatured);
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 9. getUpcomingEvents / updateUpcomingEvents
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("9. getUpcomingEvents / updateUpcomingEvents")
    class UpcomingEventsTests {

        @Test
        @DisplayName("getUpcomingEvents → chỉ trả về sự kiện chưa kết thúc")
        void getUpcomingEvents_filtersEndedEvents() {
            Event active = buildPublishedEvent(1L, "active", LocalDateTime.now().plusDays(3));
            Event ended  = buildPublishedEvent(2L, "ended",  LocalDateTime.now().minusDays(1));
            active.setUpcoming(true);
            ended.setUpcoming(true);

            when(eventRepository.findByIsUpcomingTrueAndStatusAndVisibility(
                    EventStatus.PUBLISHED, EventVisibility.PUBLIC))
                    .thenReturn(List.of(active, ended));

            List<EventResponseDTO> result = eventService.getUpcomingEvents();
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("updateUpcomingEvents với > 8 sự kiện → ném IllegalArgumentException")
        void updateUpcomingEvents_tooMany_throwsException() {
            assertThatThrownBy(() ->
                    eventService.updateUpcomingEvents(List.of(1L,2L,3L,4L,5L,6L,7L,8L,9L)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("tối đa 8");
        }

        @Test
        @DisplayName("updateUpcomingEvents thành công → trả về danh sách mới")
        void updateUpcomingEvents_success() {
            Event e1 = buildPublishedEvent(1L, "slug-1", LocalDateTime.now().plusDays(5));

            when(eventRepository.findByIsUpcomingTrue()).thenReturn(Collections.emptyList());
            when(eventRepository.findAllById(List.of(1L))).thenReturn(List.of(e1));
            when(eventRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

            List<EventResponseDTO> result = eventService.updateUpcomingEvents(List.of(1L));
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isUpcoming()).isTrue();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 10. requestEditPermission / approveEditPermission / rejectEditPermission
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("10. requestEditPermission / approveEditPermission / rejectEditPermission")
    class EditPermissionTests {

        @BeforeEach
        void setUpPublishedLockedEvent() {
            event.setStatus(EventStatus.PUBLISHED);
            event.setEditLocked(true);
            event.setEditRequestStatus(EditRequestStatus.NONE);
            event.setStartDate(LocalDateTime.now().plusDays(10)); // sự kiện chưa bắt đầu
        }

        @Test
        @DisplayName("Gửi yêu cầu chỉnh sửa hợp lệ → editRequestStatus = PENDING")
        void requestEditPermission_success() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService)
                    .sendEditRequestPendingEmail(anyString(), anyString(), anyString(), anyString());

            assertThatCode(() -> eventService.requestEditPermission(100L, "Cần sửa địa điểm"))
                    .doesNotThrowAnyException();
            assertThat(event.getEditRequestStatus()).isEqualTo(EditRequestStatus.PENDING);
        }

        @Test
        @DisplayName("Gửi yêu cầu khi đã có yêu cầu PENDING → ném IllegalArgumentException")
        void requestEditPermission_alreadyPending_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setEditRequestStatus(EditRequestStatus.PENDING);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.requestEditPermission(100L, "Lý do"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("đang có một yêu cầu");
        }

        @Test
        @DisplayName("Gửi yêu cầu với lý do rỗng → ném IllegalArgumentException")
        void requestEditPermission_emptyReason_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.requestEditPermission(100L, "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("lý do");
        }

        @Test
        @DisplayName("Gửi yêu cầu cho sự kiện chưa PUBLISHED → ném IllegalArgumentException")
        void requestEditPermission_notPublished_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStatus(EventStatus.DRAFT);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.requestEditPermission(100L, "Lý do"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ĐÃ CÔNG BỐ");
        }

        @Test
        @DisplayName("Gửi yêu cầu cho sự kiện đã bắt đầu → ném IllegalArgumentException")
        void requestEditPermission_eventAlreadyStarted_throwsException() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            event.setStartDate(LocalDateTime.now().minusHours(1)); // đã bắt đầu
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.requestEditPermission(100L, "Lý do"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("đang diễn ra");
        }

        @Test
        @DisplayName("Admin duyệt yêu cầu PENDING → editLocked = false, status = APPROVED")
        void approveEditPermission_success() {
            event.setEditRequestStatus(EditRequestStatus.PENDING);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService)
                    .sendEditRequestApprovedEmail(anyString(), anyString(), anyString(), anyString());

            assertThatCode(() -> eventService.approveEditPermission(100L))
                    .doesNotThrowAnyException();
            assertThat(event.isEditLocked()).isFalse();
            assertThat(event.getEditRequestStatus()).isEqualTo(EditRequestStatus.APPROVED);
        }

        @Test
        @DisplayName("Admin duyệt khi không có yêu cầu PENDING → ném IllegalArgumentException")
        void approveEditPermission_noPending_throwsException() {
            event.setEditRequestStatus(EditRequestStatus.NONE);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.approveEditPermission(100L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("không có yêu cầu");
        }

        @Test
        @DisplayName("Admin từ chối yêu cầu PENDING → status = REJECTED")
        void rejectEditPermission_success() {
            event.setEditRequestStatus(EditRequestStatus.PENDING);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            doNothing().when(emailService)
                    .sendEditRequestRejectedEmail(anyString(), anyString(), anyString(), anyString());

            assertThatCode(() -> eventService.rejectEditPermission(100L, "Không hợp lệ"))
                    .doesNotThrowAnyException();
            assertThat(event.getEditRequestStatus()).isEqualTo(EditRequestStatus.REJECTED);
        }

        @Test
        @DisplayName("Admin từ chối khi không có yêu cầu PENDING → ném IllegalArgumentException")
        void rejectEditPermission_noPending_throwsException() {
            event.setEditRequestStatus(EditRequestStatus.NONE);
            when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.rejectEditPermission(100L, "reason"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("không có yêu cầu");
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 11. getMyRegistrationHistory / toggleNewsletterSubscription
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("11. getMyRegistrationHistory / toggleNewsletterSubscription")
    class UserFeatureTests {

        @Test
        @DisplayName("getMyRegistrationHistory → trả về danh sách đăng ký của user hiện tại")
        void getMyRegistrationHistory_success() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            EventAttendees reg = new EventAttendees();
            reg.setId(1L);
            reg.setUser(regularUser);
            reg.setEvent(event);
            reg.setStatus(RegistrationStatus.PENDING);
            reg.setEventCheckInStatus(CheckInStatus.NOT_CHECKED_IN);
            reg.setRegistrationDate(LocalDateTime.now());

            when(eventAttendeesRepository.findByUser_IdOrderByRegistrationDateDesc(regularUser.getId()))
                    .thenReturn(List.of(reg));

            var result = eventService.getMyRegistrationHistory();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEventName()).isEqualTo(event.getEventName());
        }

        @Test
        @DisplayName("getMyRegistrationHistory không có đăng ký → trả về danh sách rỗng")
        void getMyRegistrationHistory_empty() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();

            when(eventAttendeesRepository.findByUser_IdOrderByRegistrationDateDesc(regularUser.getId()))
                    .thenReturn(Collections.emptyList());

            var result = eventService.getMyRegistrationHistory();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("toggleNewsletterSubscription(true) → user.isSubscribedNews = true")
        void toggleNewsletterSubscription_subscribe() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> eventService.toggleNewsletterSubscription(true))
                    .doesNotThrowAnyException();
            assertThat(regularUser.isSubscribedNews()).isTrue();
            verify(userRepository).save(regularUser);
        }

        @Test
        @DisplayName("toggleNewsletterSubscription(false) → user.isSubscribedNews = false")
        void toggleNewsletterSubscription_unsubscribe() {
            mockSecurityContextAsUser();
            stubGetCurrentUser();
            regularUser.setSubscribedNews(true);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            assertThatCode(() -> eventService.toggleNewsletterSubscription(false))
                    .doesNotThrowAnyException();
            assertThat(regularUser.isSubscribedNews()).isFalse();
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // 12. getEventBySlug / getAllEvents / getPublicEvents / getMyEvents
    // ════════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("12. Query methods – getEventBySlug, getAllEvents, getPublicEvents, getMyEvents")
    class QueryMethodTests {

        @Test
        @DisplayName("getEventBySlug tồn tại → trả về EventResponseDTO")
        void getEventBySlug_found() {
            when(eventRepository.findBySlug("su-kien-test")).thenReturn(Optional.of(event));
            EventResponseDTO result = eventService.getEventBySlug("su-kien-test");
            assertThat(result.getSlug()).isEqualTo("su-kien-test");
        }

        @Test
        @DisplayName("getEventBySlug không tồn tại → ném ResourceNotFoundException")
        void getEventBySlug_notFound() {
            when(eventRepository.findBySlug("abc")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> eventService.getEventBySlug("abc"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("getAllEvents → không bao gồm sự kiện DRAFT")
        void getAllEvents_excludesDraft() {
            Event published = buildPublishedEvent(1L, "published", LocalDateTime.now().plusDays(3));
            when(eventRepository.findByStatusNot(EventStatus.DRAFT)).thenReturn(List.of(published));

            List<EventResponseDTO> result = eventService.getAllEvents();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(EventStatus.PUBLISHED);
        }

        @Test
        @DisplayName("getPublicEvents → chỉ trả về sự kiện PUBLIC, PUBLISHED, chưa kết thúc")
        void getPublicEvents_filtersCorrectly() {
            Event active = buildPublishedEvent(1L, "active", LocalDateTime.now().plusDays(2));
            Event ended  = buildPublishedEvent(2L, "ended",  LocalDateTime.now().minusDays(1));

            when(eventRepository.findByStatusAndVisibility(EventStatus.PUBLISHED, EventVisibility.PUBLIC))
                    .thenReturn(List.of(active, ended));

            List<EventResponseDTO> result = eventService.getPublicEvents();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEventId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("getMyEvents → trả về sự kiện của organizer hiện tại")
        void getMyEvents_returnsOwnEvents() {
            mockSecurityContextAsOrganizer();
            stubGetCurrentOrganizer();

            when(eventRepository.findByOrganizer_OrganizerId(organizer.getOrganizerId()))
                    .thenReturn(List.of(event));

            List<EventResponseDTO> result = eventService.getMyEvents();
            assertThat(result).hasSize(1);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Utility builder
    // ────────────────────────────────────────────────────────────────────────────

    /** Tạo một Event PUBLISHED với organizer mặc định và endDate tùy chọn. */
    private Event buildPublishedEvent(Long id, String slug, LocalDateTime endDate) {
        Event e = new Event();
        e.setEventId(id);
        e.setSlug(slug);
        e.setEventName("Sự kiện " + slug);
        e.setOrganizer(organizer);
        e.setStatus(EventStatus.PUBLISHED);
        e.setVisibility(EventVisibility.PUBLIC);
        e.setStartDate(LocalDateTime.now().plusDays(1));
        e.setEndDate(endDate);
        e.setLocation("Hà Nội");
        e.setEditLocked(false);
        e.setEditRequestStatus(EditRequestStatus.NONE);
        e.setFeatured(false);
        e.setUpcoming(false);
        return e;
    }
}