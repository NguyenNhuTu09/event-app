package com.example.backend.Controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.DTO.Request.DeleteAccountRequestDTO;
import com.example.backend.DTO.Response.DeletionOtpResponseDTO;
import com.example.backend.Service.Interface.AccountDeletionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Người dùng tự xoá tài khoản (yêu cầu Google Play account deletion).
 *
 * Luồng hai bước: xin OTP qua email -> gửi OTP để xác nhận xoá.
 * Trang web xoá tài khoản khai trên Play Console dùng lại đúng hai endpoint này.
 *
 * Tách controller riêng giống UserBlockController / ContentPolicyController để
 * không phải sửa UserController. Hai đường dẫn "/me/deletion-otp" và
 * "/me/deletion" có 2 đoạn nên không đụng "/{uid}"; so với
 * "POST /{userId}/block" thì khác đoạn thứ hai.
 *
 * Không try/catch ở đây: mọi lỗi đi qua GlobalExceptionHandler và trả về cùng
 * một định dạng { status, message, timestamp }.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Account Deletion")
@SecurityRequirement(name = "bearerAuth")
public class AccountDeletionController {

    private final AccountDeletionService accountDeletionService;

    @Operation(
        summary = "Bước 1: Gửi mã OTP xác nhận xoá tài khoản",
        description = """
            Gửi mã 6 số tới email của tài khoản đang đăng nhập.

            - 200: đã gửi mã; response có expiresInSeconds và resendAfterSeconds
            - 403: tài khoản SADMIN không được xoá
            - 409: còn sự kiện đang hoạt động (message nêu tên sự kiện)
            - 429: xin mã quá nhanh, message cho biết số giây phải chờ
            """)
    @PostMapping("/me/deletion-otp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeletionOtpResponseDTO> requestDeletionOtp() {
        return ResponseEntity.ok(accountDeletionService.requestDeletionOtp());
    }

    @Operation(
        summary = "Bước 2: Xác nhận xoá tài khoản bằng OTP",
        description = """
            Xoá vĩnh viễn tài khoản đang đăng nhập. Không thể hoàn tác.

            Sau khi nhận 200, client PHẢI xoá access token / refresh token đã lưu
            và đưa người dùng về màn hình đăng nhập: mọi token cũ đều đã mất hiệu lực.

            - 200: đã xoá
            - 400: mã sai định dạng, sai giá trị (message nêu số lần thử còn lại), hết hạn, hoặc chưa xin mã
            - 403: tài khoản SADMIN không được xoá
            - 409: còn sự kiện đang hoạt động
            - 429: nhập sai quá số lần cho phép, mã đã bị huỷ, phải xin mã mới
            """)
    @PostMapping("/me/deletion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteMyAccount(
            @Valid @RequestBody DeleteAccountRequestDTO request) {

        accountDeletionService.deleteCurrentAccount(request.getOtp(), request.getReason());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("deleted", true);
        body.put("message", "Tài khoản của bạn đã được xoá. Email xác nhận đã được gửi tới hộp thư của bạn.");
        return ResponseEntity.ok(body);
    }
}