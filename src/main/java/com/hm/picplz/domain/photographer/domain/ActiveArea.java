package com.hm.picplz.domain.photographer.domain;

import com.hm.picplz.domain.area.domain.Area;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActiveArea extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "active_area_id", updatable = false)
	private Long id;

	// Photographer와의 다대일 관계
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "photographer_id")
	private Photographer photographer;

	// Area와의 다대일 관계
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "area_id")
	private Area area;

	private String sido;

	private String sigungu;

	private String eupmyeondong;

	private String ri;

	private String name;

	private Integer priority; // 작가 활동 지역 우선순위

	@Builder
	public ActiveArea(Photographer photographer, Area area, String sido, String sigungu,
		String eupmyeondong, String ri, String name, Integer priority) {
		this.photographer = photographer;
		this.area = area;
		this.sido = sido;
		this.sigungu = sigungu;
		this.eupmyeondong = eupmyeondong;
		this.ri = ri;
		this.name = name;
		this.priority = priority;
	}
}
