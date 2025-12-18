package com.example.backend;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.backend.Models.Entity.Activity;
import com.example.backend.Models.Entity.ActivityAttendees;
import com.example.backend.Models.Entity.ActivityCategories;
import com.example.backend.Models.Entity.Event;
import com.example.backend.Models.Entity.EventAttendees;
import com.example.backend.Models.Entity.Organizers;
import com.example.backend.Models.Entity.User;
import com.example.backend.Repository.ActivityAttendeesRepository;
import com.example.backend.Repository.ActivityCategoriesRepository;
import com.example.backend.Repository.ActivityRepository;
import com.example.backend.Repository.EventAttendeesRepository;
import com.example.backend.Repository.EventRepository;
import com.example.backend.Repository.OrganizersRepository;
import com.example.backend.Repository.UserRepository;
import com.example.backend.Utils.CheckInStatus;
import com.example.backend.Utils.EventStatus;
import com.example.backend.Utils.EventVisibility;
import com.example.backend.Utils.RegistrationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class CheckInIntegrationTest {

    @Autowired private MockMvc mockMvc; 
    @Autowired private ObjectMapper objectMapper; 

    @Autowired private UserRepository userRepository;
    @Autowired private OrganizersRepository organizersRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private EventAttendeesRepository eventAttendeesRepository;
    @Autowired private ActivityRepository activityRepository;
    @Autowired private ActivityAttendeesRepository activityAttendeesRepository;
    @Autowired private ActivityCategoriesRepository activityCategoriesRepository;

    private String testTicketCode;
    private String testActivityQrCode;
    
    private final String ORG_EMAIL = "organizer@test.com";
    private final String USER_EMAIL = "user@test.com";

    @BeforeEach
    public void setup() {
        // 1. Tạo Organizer
        User orgUser = new User();
        orgUser.setEmail(ORG_EMAIL);
        orgUser.setUsername("Org Test");
        orgUser.setPassword("pass");
        orgUser = userRepository.save(orgUser);

        Organizers organizer = new Organizers();
        organizer.setUser(orgUser);
        organizer.setName("Test Org");
        organizer.setApproved(true);
        organizer = organizersRepository.save(organizer);

        // 2. Tạo User tham gia
        User attendeeUser = new User();
        attendeeUser.setEmail(USER_EMAIL);
        attendeeUser.setUsername("User Test");
        attendeeUser.setPassword("pass");
        attendeeUser = userRepository.save(attendeeUser);

        // 3. Tạo Event
        Event event = new Event();
        event.setEventName("Event Test Checkin");
        event.setOrganizer(organizer);
        event.setSlug("test-slug");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setStatus(EventStatus.PUBLISHED);
        event.setVisibility(EventVisibility.PUBLIC);
        event.setLocation("Hanoi");
        event = eventRepository.save(event);

        // 4. Tạo Vé (Đã Approve)
        EventAttendees ticket = new EventAttendees();
        ticket.setEvent(event);
        ticket.setUser(attendeeUser);
        ticket.setStatus(RegistrationStatus.APPROVED);
        ticket = eventAttendeesRepository.save(ticket);
        this.testTicketCode = ticket.getTicketCode(); // Lưu lại code để test

        // 5. Tạo Category & Activity
        ActivityCategories category = new ActivityCategories();
        category.setCategoryName("Workshop");
        category = activityCategoriesRepository.save(category);

        Activity activity = new Activity();
        activity.setActivityName("Activity 1");
        activity.setEvent(event);
        activity.setCategory(category);
        activity.setStartTime(LocalDateTime.now().minusHours(1)); // Đang diễn ra
        activity.setEndTime(LocalDateTime.now().plusHours(1));
        activity = activityRepository.save(activity);
        this.testActivityQrCode = activity.getActivityQrCode(); // Lưu lại code để test

        // 6. Đăng ký Activity cho User
        ActivityAttendees actAttendee = new ActivityAttendees();
        actAttendee.setEventAttendee(ticket);
        actAttendee.setActivity(activity);
        activityAttendeesRepository.save(actAttendee);
    }

    // --- TEST CASE 1: ORGANIZER CHECK-IN USER ---
    @Test
    @WithMockUser(username = ORG_EMAIL, authorities = {"ORGANIZER"}) // Giả lập đang đăng nhập là Organizer
    public void testOrganizerCheckInSuccess() throws Exception {
        // Tạo JSON Body
        String jsonBody = "{\"ticketCode\": \"" + testTicketCode + "\"}";

        // Thực hiện Request
        mockMvc.perform(post("/api/checkin/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                // Kiểm tra kết quả mong đợi
                .andExpect(status().isOk()) // Mong đợi HTTP 200
                .andExpect(jsonPath("$.email").value(USER_EMAIL)) // Mong đợi trả về đúng email user
                .andExpect(jsonPath("$.eventCheckInStatus").value("CHECKED_IN")); // Mong đợi trạng thái đổi thành CHECKED_IN
    }

    // --- TEST CASE 2: USER TỰ CHECK-IN ACTIVITY ---
    @Test
    @WithMockUser(username = USER_EMAIL, authorities = {"USER"}) // Giả lập đang đăng nhập là User
    public void testUserCheckInActivitySuccess() throws Exception {
        // Trước tiên, User phải Check-in vào Event trước (Logic bắt buộc)
        // Ta set cứng trong DB là đã check-in event
        EventAttendees ticket = eventAttendeesRepository.findByTicketCode(testTicketCode).get();
        ticket.setEventCheckInStatus(CheckInStatus.CHECKED_IN);
        eventAttendeesRepository.save(ticket);

        // Tạo JSON Body
        String jsonBody = "{\"activityQrCode\": \"" + testActivityQrCode + "\"}";

        // Thực hiện Request
        mockMvc.perform(post("/api/checkin/activity")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                // Kiểm tra kết quả
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Điểm danh thành công hoạt động: Activity 1"));
    }
    
    // --- TEST CASE 3: CHECK-IN THẤT BẠI (Sai quyền) ---
    @Test
    @WithMockUser(username = "hacker@test.com", authorities = {"USER"})
    public void testUnauthorizedCheckIn() throws Exception {
        // User thường cố tình gọi API check-in của Organizer -> Phải bị chặn
        String jsonBody = "{\"ticketCode\": \"" + testTicketCode + "\"}";

        mockMvc.perform(post("/api/checkin/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isForbidden()); // Mong đợi lỗi 403 Forbidden
    }
}