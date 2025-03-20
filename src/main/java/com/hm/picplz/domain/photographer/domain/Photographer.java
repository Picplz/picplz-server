package com.hm.picplz.domain.photographer.domain;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.product.domain.ShootProduct;
import com.hm.picplz.domain.review.domain.Review;
import com.hm.picplz.global.common.entity.BaseEntity;
import com.hm.picplz.global.common.entity.YesNo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photographer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photographer_id", updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "member_id")
    private Member member;

    @NotNull
    private String area;

    private int period; // 1년 3개월 -> 15개월

    private YesNo active;  // Y/N

    private String instagram; // 인스타그램 아이디

    private String introduction; // 소갯말

    @OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<ShootProduct> shootProducts = new ArrayList<>();

    @OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<PhotoMood> photoMoods = new ArrayList<>();

    @OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<Career> careers = new ArrayList<>();

    @Builder
    private Photographer(Long id, Member member, String area,int period, YesNo active,
        String instagram, String introduction) {
        this.id = id;
        this.member = member;
        this.area = area;
        this.period = period;
        this.active = active;
        this.instagram = instagram;
        this.introduction = introduction;
    }
}
