package com.example.backend.Service.Interface;

import com.example.backend.DTO.Response.DeletionOtpResponseDTO;

/**
 * Xoá tài khoản theo yêu cầu Google Play (account deletion).
 *
 * "Xoá" ở đây là ẩn danh hoá: dòng users được giữ để các khoá ngoại
 * (event_attendees, moment_reports...) không vỡ, nhưng mọi dữ liệu cá nhân
 * bị xoá và tài khoản không đăng nhập lại được.
 */
public interface AccountDeletionService {

    /**
     * Gửi OTP xác nhận xoá tới email của người dùng hiện tại.
     *
     * @return thời hạn mã và thời gian chờ trước khi được xin mã mới
     *
     * @throws com.example.backend.Exception.ForbiddenException              SADMIN (403)
     * @throws com.example.backend.Exception.AccountDeletionBlockedException  còn sự kiện đang hoạt động (409)
     * @throws com.example.backend.Exception.TooManyRequestsException         xin mã trong thời gian chờ (429)
     */
    DeletionOtpResponseDTO requestDeletionOtp();

    /**
     * Người dùng hiện tại tự xoá tài khoản sau khi nhập đúng OTP.
     *
     * @param otp    mã 6 số trong email
     * @param reason lý do (tuỳ chọn), chỉ ghi log
     * @throws com.example.backend.Exception.InvalidOtpException              OTP sai / hết hạn / chưa cấp (400)
     * @throws com.example.backend.Exception.TooManyRequestsException         nhập sai quá số lần (429)
     * @throws com.example.backend.Exception.ForbiddenException              SADMIN (403)
     * @throws com.example.backend.Exception.AccountDeletionBlockedException  còn sự kiện đang hoạt động (409)
     */
    void deleteCurrentAccount(String otp, String reason);

    /**
     * SADMIN xoá tài khoản của người khác. Không cần OTP, cùng quy tắc chặn.
     *
     * @throws com.example.backend.Exception.ResourceNotFoundException        không tồn tại hoặc đã xoá (404)
     */
    void deleteAccountByAdmin(String uid);
}