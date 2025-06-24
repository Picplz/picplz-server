package com.hm.picplz.domain.portfolio.domain;

import com.hm.picplz.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioPhoto extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_photo_id", updatable = false)
    private Long id;

    @NotNull
    private String image;

    @Column(name = "photo_order")
    private int photoOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Builder
    private PortfolioPhoto(Long id, String image, int photoOrder, Portfolio portfolio) {
        this.id = id;
        this.image = image;
        this.photoOrder = photoOrder;
        this.portfolio = portfolio;
    }
    // factory method
}
