package com.hm.picplz.domain.reservation.exception;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode implements BaseErrorCode {

    RESERVATION_NOT_FOUND(NOT_FOUND, "RESERVATION_404_1", "해당 예약 내역이 존재하지 않습니다."),
    RESERVATION_STATUS_INVALID(BAD_REQUEST, "RESERVATION_400_1", "현재 상태에서는 해당 요청을 처리할 수 없습니다.")
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
