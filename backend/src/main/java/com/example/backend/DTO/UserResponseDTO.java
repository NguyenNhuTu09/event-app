package com.example.backend.DTO;

import java.time.LocalDate;

import com.example.backend.Models.Gender;
import com.example.backend.Utils.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    /**
     * Khoá chính dạng số.
     *
     * Client cần trường này để biết bài viết nào là của chính mình:
     * MomentResponseDTO.userId là số, trong khi trước đây /users/me chỉ trả uid
     * dạng UUID nên không đối chiếu được. Hệ quả là app hiện nút "Báo cáo" trên
     * bài của chính người dùng, và chủ bài không nhận ra bài UNDER_REVIEW của
     * mình — vi phạm đúng hành vi Google yêu cầu.
     *
     * uid vẫn giữ nguyên cho các endpoint quản trị đang dùng nó.
     */
    private Long id;

    private String uid;
    private String username;
    private String email;
    private String address;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String avatarUrl;
    private Role role;

    /**
     * Phiên bản Quy tắc cộng đồng người dùng đã đồng ý. NULL = chưa đồng ý
     * lần nào; client mobile dùng giá trị này để quyết định có hiện màn
     * Quy tắc cộng đồng trước lần đăng bài đầu tiên hay không.
     */
    private String contentPolicyAcceptedVersion;

    /**
     * Constructor 9 tham số giữ lại có chủ ý.
     *
     * AuthService đang gọi new UserResponseDTO(uid, username, email, address,
     * gender, dateOfBirth, phoneNumber, avatarUrl, role) ở vài chỗ. Nếu chỉ
     * thêm field mới thì @AllArgsConstructor sẽ sinh constructor 11 tham số và
     * toàn bộ những lời gọi đó vỡ. Giữ overload này để không phải sửa
     * AuthService.
     *
     * Payload đăng nhập vì vậy có id = null và contentPolicyAcceptedVersion =
     * null. Frontend đã xác nhận không cần hai trường này lúc đăng nhập, họ lấy
     * từ GET /users/me.
     */
    public UserResponseDTO(String uid, String username, String email, String address,
                           Gender gender, LocalDate dateOfBirth, String phoneNumber,
                           String avatarUrl, Role role) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}