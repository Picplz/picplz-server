package com.hm.picplz.domain.portfolio.domain;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.global.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id")
    private Photographer photographer;

    private String location;

    private LocalDate uploadDate;

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<PortfolioPhoto> portfolioPhotos = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Scrap> scraps = new ArrayList<>();

    @Builder
    public Portfolio(Long id, Photographer photographer, String location, LocalDate uploadDate) {
        this.id = id;
        this.photographer = photographer;
        this.location = location;
        this.uploadDate = uploadDate;
    }

    public void addAllPortfolioPhotos(List<PortfolioPhoto> portfolioPhotos) {
        this.portfolioPhotos.addAll(portfolioPhotos);
    }

    public void addScrap(Scrap scrap) {
        scraps.add(scrap);
    }
}
