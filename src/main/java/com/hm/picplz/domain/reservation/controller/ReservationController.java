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
            @PathVariable(name = "reservationId") Long resrvationId
    ) {
        log.info("예약 상품 상세 조회하기");
        return reservationService.findReservationDetailById(resrvationId);
    }

}
