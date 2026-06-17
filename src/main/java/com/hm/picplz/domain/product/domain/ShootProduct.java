package com.hm.picplz.domain.product.domain;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.product.dto.ProductDto;
import com.hm.picplz.global.common.entity.BaseEntity;
import com.hm.picplz.global.common.entity.YesNo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShootProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shoot_product_id", updatable = false)
    private Long id;

    @NotNull
    private String name;

    private String description;

    @Positive
    @NotNull
    private int shootPrice;

    private int shootDuration;    // enum (ex. 10분 이하) 분단위

    @Positive
    @NotNull
    private int amount;

    @NotNull
    private YesNo editedYn; // 보정 유무

    @Positive
    @NotNull
    private int editPrice;

    private String otherDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographer;

    @Builder
    private ShootProduct(YesNo editedYn, Long id, String name, String description, int shootPrice,
        int shootDuration, int amount, int editPrice, String otherDetails,
        Photographer photographer) {
        this.editedYn = editedYn;
        this.id = id;
        this.name = name;
        this.description = description;
        this.shootPrice = shootPrice;
        this.shootDuration = shootDuration;
        this.amount = amount;
        this.editPrice = editPrice;
        this.otherDetails = otherDetails;
        this.photographer = photographer;
    }

    public static ShootProduct of(ProductDto.Create request, Photographer photographer) {
        return ShootProduct.builder()
                .name(request.getName())
                .description(request.getDescription())
                .shootPrice(request.getShootPrice())
                .shootDuration(request.getShootDuration())
                .amount(request.getAmount())
                .editedYn(YesNo.parse(request.getEditedYn()))
                .editPrice(request.getEditPrice())
                .otherDetails(request.getOtherDetails())
                .photographer(photographer)
                .build();
    }
}
