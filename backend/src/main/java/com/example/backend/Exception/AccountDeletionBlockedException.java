package com.example.backend.Exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ném ra khi tài khoản chưa được phép xoá vì còn sở hữu sự kiện đang hoạt động
 * (PENDING_APPROVAL / PUBLISHED / IN_PROGRESS, chưa kết thúc).
 *
 * Trả 409 thông qua @ResponseStatus — cần bản GlobalExceptionHandler đã sửa
 * trong cùng đợt này, nếu không handler Exception.class sẽ biến nó thành 500.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class AccountDeletionBlockedException extends RuntimeException {

    /** Số tên sự kiện tối đa đưa vào message, tránh message dài vô hạn. */
    private static final int MAX_NAMES_IN_MESSAGE = 3;

    private final List<String> blockingEventNames;

    public AccountDeletionBlockedException(List<String> blockingEventNames) {
        super(buildMessage(blockingEventNames));
        this.blockingEventNames = List.copyOf(blockingEventNames);
    }

    public List<String> getBlockingEventNames() {
        return blockingEventNames;
    }

    private static String buildMessage(List<String> names) {
        int shown = Math.min(MAX_NAMES_IN_MESSAGE, names.size());
        String listed = String.join(", ", names.subList(0, shown));
        String more = names.size() > shown
                ? " và " + (names.size() - shown) + " sự kiện khác"
                : "";
        return "Không thể xoá tài khoản khi còn sự kiện đang hoạt động: " + listed + more
                + ". Vui lòng hoàn tất hoặc huỷ các sự kiện này, hoặc liên hệ quản trị viên.";
    }
}