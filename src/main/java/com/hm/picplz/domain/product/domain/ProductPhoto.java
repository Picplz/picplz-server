package com.hm.picplz.domain.product.domain;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.product.dto.ProductDto;
import com.hm.picplz.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPhoto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_photo_id", updatable = false)
    private Long id;

    private String imageData;

    @Column(name = "photo_order")
    private int photoOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shoot_product")
    private ShootProduct shootProduct;

    @Builder
    private ProductPhoto(Long id, String imageData, int photoOrder, ShootProduct shootProduct) {
        this.id = id;
        this.imageData = imageData;
        this.photoOrder = photoOrder;
        this.shootProduct = shootProduct;
    }

    // factory method
    public static ProductPhoto of(String photo, int photoOrder, ShootProduct shootProduct) {
        return ProductPhoto.builder()
                .imageData(photo)
                .photoOrder(photoOrder)
                .shootProduct(shootProduct)
                .build();
    }
}
