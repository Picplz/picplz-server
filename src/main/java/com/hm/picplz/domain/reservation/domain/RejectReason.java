package com.hm.picplz.domain.reservation.domain;

public enum RejectReason {
    REGION_UNAVAILABLE,      // 해당 지역은 촬영이 어려워요
    TIME_UNAVAILABLE,        // 희망한 시간대에 촬영이 어려워요
    EQUIPMENT_ISSUE        // 장비 문제로 촬영이 불가능해요
}
