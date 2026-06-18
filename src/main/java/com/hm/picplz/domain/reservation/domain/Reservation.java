package com.hm.picplz.domain.reservation.domain;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.product.domain.ShootProduct;
import com.hm.picplz.domain.product.dto.ProductDto;
import com.hm.picplz.domain.reservation.dto.ReservationDto;
import com.hm.picplz.domain.reservation.exception.ReservationErrorCode;
import com.hm.picplz.global.common.entity.BaseEntity;
import com.hm.picplz.global.common.entity.YesNo;
import com.hm.picplz.global.error.ExceptionFactory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id", updatable = false)
    private Long id;

    private String packageName;

    private int productPrice;   // 상품 금액

    private int editPrice;  // 보정 금액

    private LocalDateTime reservationTime;

    private String place;

    @NotNull
    @Positive
    private int photoAmount;

    private YesNo editedYn;

    private String reservationNumber;   // ex) N2025194926

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private LocalDateTime statusChangedAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "reservation_reject_reasons",
            joinColumns = @JoinColumn(name = "reservation_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "reject_reason")
    private List<RejectReason> rejectReason = new ArrayList<>();

    @Column(length = 500)
    private String rejectReasonDetail;

    @Enumerated(EnumType.STRING)
    private CancelType cancelType;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "reservation_cancel_reasons",
            joinColumns = @JoinColumn(name = "reservation_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason")
    private List<CancelReason> cancelReasons = new ArrayList<>();


    @Column(length = 500)
    private String cancelReasonDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoot_product_id", nullable = false)
    private ShootProduct shootProduct;

    @Builder
    public Reservation(Long id, String packageName, int productPrice, int editPrice, LocalDateTime reservationTime, String place, int photoAmount, YesNo editedYn, String reservationNumber, ReservationStatus status, Customer customer, ShootProduct shootProduct) {
        this.id = id;
        this.packageName = packageName;
        this.productPrice = productPrice;
        this.editPrice = editPrice;
        this.reservationTime = reservationTime;
        this.place = place;
        this.photoAmount = photoAmount;
        this.editedYn = editedYn;
        this.reservationNumber = reservationNumber;
        this.status = status;
        this.customer = customer;
        this.shootProduct = shootProduct;
    }

    public static Reservation of(ReservationDto.Create request, Customer customer, ShootProduct shootProduct) {
        return Reservation.builder()
                .packageName(shootProduct.getName())
                .productPrice(shootProduct.getShootPrice())
                .editPrice(shootProduct.getEditPrice())
                .reservationTime(
                        request.getPerferredDateTimeSelected() == YesNo.Y
                                ? LocalDateTime.of(request.getReservedDate(), request.getReservedTime())
                                : null
                ) // 희망일시 체크하면 입력한 시간대로, 작가와 협의 체크하면 자동으로 null
                .place(request.getShootArea() + request.getShootAreaDetail())
                .photoAmount(shootProduct.getAmount())
                .editedYn(shootProduct.getEditedYn())
                .reservationNumber(generateReservationNumber())
                .status(ReservationStatus.PENDING) // 기본은 대기 상태
                .customer(customer)
                .shootProduct(shootProduct)
                .build();
    }

    private static String generateReservationNumber() {
        return "N" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public void accept() {
        validatePending();
        this.statusChangedAt = LocalDateTime.now();
        this.status = ReservationStatus.ACCEPTED;
    }

    public void reject(List<RejectReason> rejectReasons, String rejectReasonDetail) {
        validatePending();
        this.rejectReason = rejectReasons;
        this.statusChangedAt = LocalDateTime.now();
        this.status = ReservationStatus.REJECTED;
        this.rejectReasonDetail = rejectReasonDetail;
    }

    public void confirm(LocalDateTime confirmedDateTime) {
        if (this.status != ReservationStatus.ACCEPTED) {
            throw ExceptionFactory.of(ReservationErrorCode.RESERVATION_STATUS_INVALID);
        }
        this.statusChangedAt = LocalDateTime.now();
        this.reservationTime = confirmedDateTime;
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel(CancelType cancelType,
                       List<CancelReason> cancelReasons,
                       String cancelReasonDetail) {
        if (this.status == ReservationStatus.REJECTED || this.status == ReservationStatus.CANCELED) {
            throw ExceptionFactory.of(ReservationErrorCode.RESERVATION_STATUS_INVALID);
        }
        this.status = ReservationStatus.CANCELED;
        this.cancelType = cancelType;
        this.cancelReasons = cancelReasons;
        this.cancelReasonDetail = cancelReasonDetail;
        this.statusChangedAt = LocalDateTime.now();
    }

    private void validatePending() {
        if (this.status != ReservationStatus.PENDING) {
            throw ExceptionFactory.of(ReservationErrorCode.RESERVATION_STATUS_INVALID);
        }
    }


    // factory method

}
