package com.hm.picplz.domain.reservation.controller;

import com.hm.picplz.domain.product.dto.ProductDto;
import com.hm.picplz.domain.reservation.dto.ReservationDto;
import com.hm.picplz.domain.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "reservations")
@Tag(name = "Reservation")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "상품 예약 생성")
    @PostMapping
    public ReservationDto.ReservationId createReservation(
            @AuthenticationPrincipal Long memberId,
            @RequestBody ReservationDto.Create createReservationRequest
    ) {
        log.info("상품 예약 생성하기");
        return reservationService.createReservation(memberId, createReservationRequest);
    }


    @Operation(summary = "예약 내역 상세 조회")
    @GetMapping("/{reservationId}")
    public ReservationDto.Detail loadReservationtDetailById(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "reservationId") Long resrvationId
    ) {
        log.info("예약 상품 상세 조회하기");
        return reservationService.findReservationDetailById(memberId, resrvationId);
    }

    @Operation(summary = "작가의 예약 승인")
    @PatchMapping("/{reservationId}/accept")
    public void approveReservationsByPhotographerId(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "reservationId") Long reservationId
    ) {
        log.info("작가의 예약 내역 승인");
        reservationService.acceptReservationById(memberId, reservationId);
    }

    @Operation(summary = "작가의 예약 거절")
    @PatchMapping("/{reservationId}/reject")
    public ReservationDto.RejectReservationResponse declineReservationsByPhotographerId(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "reservationId") Long reservationId,
            @RequestBody ReservationDto.RejectReservationRequest rejectReservationRequest
    ) {
        log.info("작가의 예약 내역 거절");
        return reservationService.rejectReservationById(memberId, reservationId, rejectReservationRequest);
    }

    @Operation(summary = "고객의 예약 일자 변경 및 확정")
    @PatchMapping("/{reservationId}/confirm")
    public ReservationDto.ConfirmReservationResponse confrimReservationsByPhotographerId(
            @AuthenticationPrincipal Long memberId,
            @PathVariable(name = "reservationId") Long reservationId,
            @RequestBody ReservationDto.ConfirmReservationRequest confirmReservationRequest
    ) {
        log.info("고객의 예약 일자 변경 및 확정");
        return reservationService.confirmReservationById(memberId, reservationId, confirmReservationRequest);
    }

}
