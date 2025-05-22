package com.hm.picplz.domain.area.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;

import com.hm.picplz.domain.photographer.domain.ActiveArea;
import com.hm.picplz.global.common.entity.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Area extends BaseEntity {
	@Id
	@Column(name = "area_id", updatable = false)
	private Long id;

	private String sido;

	private String sigungu;

	private String eupmyeondong;

	private String ri;
	/**
	 * 법정동 전체 주소
	 * 참고: ngram 인덱스를 수동으로 추가해야 함
	 *       ALTER TABLE area ADD FULLTEXT INDEX idx_name_ngram (name) WITH PARSER ngram;
	 */
	//
	private String name;

	@Column(name = "area_order") // 데이터에 존재하는 '순위'
	private Integer areaOrder;

	@Column(name = "created_date")
	private LocalDate createdDate;

	@Column(name = "deleted_date")
	private LocalDate deletedDate;

	@Column(name = "old_code")
	private Long oldCode;

	// 위도, 경도 수동 입력 필요
	private Double latitude;

	private Double longitude;

	/**
	 * 위치 정보 저장 필드 (위도, 경도)
	 * SRID 4326 기반 POINT 타입
	 * 참고: 공간 인덱스를 수동으로 추가해야 함
	 *       ALTER TABLE area ADD SPATIAL INDEX(location);
	 */
	@Column(columnDefinition = "POINT SRID 4326", nullable = false)
	private Point location;

	@OneToMany(mappedBy = "area", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<ActiveArea> activeAreas = new ArrayList<>();

}
