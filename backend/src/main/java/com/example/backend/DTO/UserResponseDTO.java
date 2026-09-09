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
     * thêm field mới thì @AllArgsConstructor sẽ sinh constructor 10 tham số và
     * toàn bộ những lời gọi đó vỡ. Giữ overload này để không phải sửa
     * AuthService.
     *
     * Payload đăng nhập vì vậy sẽ có contentPolicyAcceptedVersion = null.
     * Không sao: client lấy giá trị thật từ GET /api/users/me. Nếu muốn trả
     * luôn trong lúc đăng nhập thì chuyển các lời gọi trong AuthService sang
     * dùng builder.
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