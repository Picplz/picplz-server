package com.hm.picplz.domain.reservation.service;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.customer.exception.CustomerErrorCode;
import com.hm.picplz.domain.customer.repository.CustomerRepository;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.domain.product.domain.ShootProduct;
import com.hm.picplz.domain.product.exception.ProductErrorCode;
import com.hm.picplz.domain.product.repository.ProductRepository;
import com.hm.picplz.domain.reservation.domain.Reservation;
import com.hm.picplz.domain.reservation.dto.ReservationDto;
import com.hm.picplz.domain.reservation.exception.ReservationErrorCode;
import com.hm.picplz.domain.reservation.repository.ReservationRepository;
import com.hm.picplz.global.error.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PhotographerRepository photographerRepository;

    @Transactional
    public ReservationDto.ReservationId createReservation(Long memberId, ReservationDto.Create request) {
        Customer customer = customerRepository.findByMemberId(memberId)
                .orElseThrow(() -> ExceptionFactory.of(CustomerErrorCode.CUSTOMER_NOT_FOUND));

        ShootProduct shootProduct = productRepository.findById(request.getShootProductId())
                .orElseThrow(() ->  ExceptionFactory.of(ProductErrorCode.PRODUCT_NOT_FOUND));
        Reservation reservation = Reservation.of(request, customer, shootProduct);
        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationDto.ReservationId.of(savedReservation.getId());
    }

    public ReservationDto.Detail findReservationDetailById(Long memberId, Long reservationId) {
        photographerRepository.findByMemberId(memberId)
                .orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ExceptionFactory.of(ReservationErrorCode.RESERVATION_NOT_FOUND));
        return ReservationDto.Detail.of(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDto.Detail> findReservationsByPhotographerId(Long memberId) {
        photographerRepository.findByMemberId(memberId)
                .orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));

        return reservationRepository.findAllByPhotographerMemberId(memberId).stream()
                .map(ReservationDto.Detail::of)
                .toList();
    }

    @Transactional
    public void acceptReservationById(Long memberId, Long reservationId) {
        photographerRepository.findByMemberId(memberId)
                .orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ExceptionFactory.of(ReservationErrorCode.RESERVATION_NOT_FOUND));
        reservation.accept();
    }

    @Transactional
    public ReservationDto.RejectReservationResponse rejectReservationById(Long memberId, Long reservationId,
                                                                          ReservationDto.RejectReservationRequest rejectReservationRequest) {
        photographerRepository.findByMemberId(memberId)
                .orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ExceptionFactory.of(ReservationErrorCode.RESERVATION_NOT_FOUND));
        reservation.reject(rejectReservationRequest.getRejectReason());
        return ReservationDto.RejectReservationResponse.of(reservation);
    }

    @Transactional
    public ReservationDto.ConfirmReservationResponse confirmReservationById(Long memberId, Long reservationId,
                                                                          ReservationDto.ConfirmReservationRequest confirmReservationRequest) {
        customerRepository.findByMemberId(memberId)
                .orElseThrow(() -> ExceptionFactory.of(CustomerErrorCode.CUSTOMER_NOT_FOUND));
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ExceptionFactory.of(ReservationErrorCode.RESERVATION_NOT_FOUND));
        reservation.confirm(LocalDateTime.of(confirmReservationRequest.getReservedDate(), confirmReservationRequest.getReservedTime()));
        return ReservationDto.ConfirmReservationResponse.of(reservation);
    }
}
