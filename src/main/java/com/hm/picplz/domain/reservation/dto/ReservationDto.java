package com.hm.picplz.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hm.picplz.domain.product.domain.ProductPhoto;
import com.hm.picplz.domain.product.domain.ShootProduct;
import com.hm.picplz.domain.product.dto.ProductDto;
import com.hm.picplz.domain.reservation.domain.Reservation;
import com.hm.picplz.domain.reservation.domain.ReservationStatus;
import com.hm.picplz.global.common.entity.YesNo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationDto {
// 1번 shootProductId 2번 shootArea : { ~~ }, 3번 shootAreaDetail : "~~", 4번 preferredDateTimeSelected 5번 reservedDate, 6번 reservedTime

    @Data
    @NoArgsConstructor
    public static class Create {

        private Long shootProductId;
        private String shootArea;
        private String shootAreaDetail;
        private YesNo perferredDateTimeSelected;

        @Schema(type = "string", example = "2026-01-27", format = "date")
        private LocalDate reservedDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @Schema(type = "string", example = "15:30", pattern = "HH:mm")
        private LocalTime reservedTime;
    }

    @Data
    @NoArgsConstructor
    public static class ReservationId {

        private Long id;

        public static ReservationId of(Long id) {
            ReservationId reservationId = new ReservationId();
            reservationId.id = id;

            return reservationId;
        }
    }


    @Data
    @NoArgsConstructor
    public static class Detail {
        private Long reservationId;
        private String packageName;
        private int productPrice;   // 상품 금액
        private int editPrice;  // 보정 금액
        private LocalDateTime reservationTime;
        private String place;
        private int photoAmount;
        private YesNo editedYn;
        private String reservationNumber;   // ex) N2025194926
        private ReservationStatus status;

        public static Detail of(Reservation reservation) {
            Detail detail = new Detail();
            detail.reservationId = reservation.getId();
            detail.packageName = reservation.getPackageName();
            detail.productPrice = reservation.getProductPrice();
            detail.editPrice = reservation.getEditPrice();
            detail.reservationTime = reservation.getReservationTime();
            detail.place = reservation.getPlace();
            detail.photoAmount = reservation.getPhotoAmount();
            detail.editedYn = reservation.getEditedYn();
            detail.reservationNumber = reservation.getReservationNumber();
            detail.status = reservation.getStatus();
            return detail;
        }
    }

    @Data
    @NoArgsConstructor
    public static class RejectReservationRequest {
        private String rejectReason;
    }

    @Data
    @NoArgsConstructor
    public static class RejectReservationResponse {
        private String rejectReason;

        public static RejectReservationResponse of(RejectReservationRequest rejectReservationRequest) {
            RejectReservationResponse rejectReservationResponse = new RejectReservationResponse();
            rejectReservationResponse.rejectReason = rejectReservationRequest.rejectReason;
            return rejectReservationResponse;
        }
    }
}
