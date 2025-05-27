package com.hm.picplz.domain.area.dto;

import com.hm.picplz.domain.area.domain.Area;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AreaDto {

	@Data
	@NoArgsConstructor
	public static class AreaInfo {
		private Long id;
		private String name;
		private String dong;
		private String ri;

		public static AreaInfo from(Area area) {
			AreaInfo areaInfo = new AreaInfo();
			areaInfo.id = area.getId();
			areaInfo.name = area.getName();
			areaInfo.dong = area.getEupmyeondong();
			areaInfo.ri = area.getRi();
			return areaInfo;
		}
	}
}
