package com.hm.picplz.domain.photographer.domain;

import com.hm.picplz.global.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기획서에 적힌 촬영 기기 목록 저장 테이블. 조회용 데이터.
 * 작가의 카메라와 연결짓지 않음. 작가가 직접 입력한 카메라를 다른 작가에게 보여줄 이유가 없기 때문에.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultCamera extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "default_camera_id", updatable = false)
	private Long id;

	private String type; // 핸드폰, 카메라
	private String brand; // 애플, 삼성, 소니 ...
	private String name; // 모델명
}
