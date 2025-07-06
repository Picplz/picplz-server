package com.hm.picplz.domain.photographer.domain;

import com.hm.picplz.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class PhotographerCamera extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "photographer_camera_id", updatable = false)
	private Long id;

	private String type; // 핸드폰, 카메라
	private String brand; // 직접 입력 가능, 애플, 삼성, 소니
	private String name; // 직접 입력 가능, 모델명
	private String cameraType; // DSLR, 필름 등등...

	@ManyToOne
	@JoinColumn(name = "photographer_id")
	private Photographer photographer;

	@Builder
	public PhotographerCamera(Long id, String type, String brand, String name, String cameraType, Photographer photographer) {
		this.id = id;
		this.type = type;
		this.brand = brand;
		this.name = name;
		this.cameraType = cameraType;
		this.photographer = photographer;
	}
}
