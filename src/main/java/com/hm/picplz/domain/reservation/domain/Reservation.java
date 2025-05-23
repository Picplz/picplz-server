package com.hm.picplz.domain.reservation.domain;

import com.hm.picplz.global.common.entity.BaseEntity;
import com.hm.picplz.global.common.entity.YesNo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id", updatable = false)
    private Long id;

    private String packageName;   // enum

    private String companion;

    @NotNull
    @PositiveOrZero
    private int price;  // 최종 결제 금액

    private int productPrice;   // 상품 금액

    private int editPrice;  // 보정 금액

    private LocalDateTime reservationTime;

    private String place;

    private String camera;

    @NotNull
    @Positive
    private String photoAmount;

    private YesNo editedYn;

    private String paymentType;    // enum

    private String reservationNumber;   // ex) N2025194926

    @Builder
    public Reservation(Long id, String packageName, String companion, int price, int productPrice, int editPrice, LocalDateTime reservationTime, String place, String camera, String photoAmount, YesNo editedYn, String paymentType, String reservationNumber) {
        this.id = id;
        this.packageName = packageName;
        this.companion = companion;
        this.price = price;
        this.productPrice = productPrice;
        this.editPrice = editPrice;
        this.reservationTime = reservationTime;
        this.place = place;
        this.camera = camera;
        this.photoAmount = photoAmount;
        this.editedYn = editedYn;
        this.paymentType = paymentType;
        this.reservationNumber = reservationNumber;
    }

    // factory method
}
