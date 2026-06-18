package com.hm.picplz.domain.reservation.domain;

public enum CancelReason {
    // 고객 측 취소 사유
    SCHEDULE_CONFLICT,          // 갑작스럽게 다른 일정이 생겼어요
    PRODUCT_CHANGE_REQUEST,     // 촬영 상품을 변경하고 싶어요
    LOCATION_ISSUE,             // 장소에 문제가 생겼어요
    CHANGE_OF_MIND,             // 단순 변심
    PHOTOGRAPHER_NO_RESPONSE,   // 작가의 답변이 늦거나 불확실했어요
    ADDITIONAL_PAYMENT_REQUEST, // 작가가 추가 결제를 유도해요

    // 작가 측 취소 사유
    HEALTH_ISSUE,               // 컨디션 또는 건강 문제로 촬영이 어려워졌어요
    CUSTOMER_NO_RESPONSE,       // 고객의 답변이 늦거나 불확실했어요
    ACCIDENTAL_ACCEPTANCE,      // 실수로 예약을 수락했어요
    EQUIPMENT_FAILURE,          // 장비 문제로 촬영이 불가능해졌어요
    EXTERNAL_CIRCUMSTANCE       // 날씨, 장소 확보 등 외부 요인으로 진행이 어려워요
}
