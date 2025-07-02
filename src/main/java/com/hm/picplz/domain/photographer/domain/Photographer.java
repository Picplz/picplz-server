package com.hm.picplz.domain.photographer.domain;

import java.util.ArrayList;
import java.util.List;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.product.domain.ShootProduct;
import com.hm.picplz.domain.review.domain.Review;
import com.hm.picplz.global.common.entity.BaseEntity;
import com.hm.picplz.global.common.entity.YesNo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	private YesNo active;  // Y/N

	// 패키지
	@OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<ShootProduct> shootProducts = new ArrayList<>();

	@OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<Review> reviews = new ArrayList<>();

	// 분위기 키워드
	@OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
	private List<PhotoMood> photoMoods = new ArrayList<>();

	// 주 촬영지
	@OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
	private List<ActiveArea> activeAreas = new ArrayList<>();

	// 촬영 기기
	@OneToMany(mappedBy = "photographer", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, orphanRemoval = true)
	private List<PhotographerCamera> cameras = new ArrayList<>();

	@Builder
	public static Photographer from (Member member) {
		Photographer photographer = new Photographer();
		photographer.member = member;
		photographer.active = YesNo.N;
		return photographer;
	}

	public void addAllPhotoMoods(List<PhotoMood> photoMoods) {
		this.photoMoods.addAll(photoMoods);
	}

	public void addAllActiveAreas(List<ActiveArea> activeAreas) {
		this.activeAreas.addAll(activeAreas);
	}

	public void removeAllActiveArea() {
		this.activeAreas.clear();
	}

	public void addAllCameras(List<PhotographerCamera> cameras) {
		this.cameras.addAll(cameras);
	}
}
