package com.hm.picplz.domain.reservation.domain;

public enum ReservationStatus {
    PENDING,    // 예약 대기 (고객 신청 완료, 작가 미확인)
    APPROVED,   // 예약 승인 (작가 수락)
    REJECTED,    // 예약 거절 (작가 거절)
    CANCELED     // 예약 취소 (작가 자동 취소, 고객 취소, 결제 후 환불)
}