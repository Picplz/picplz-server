package com.hm.picplz.domain.area.dto;

import com.hm.picplz.domain.area.domain.Area;

import lombok.*;

import java.util.List;

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

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AllAreaInfo {
		private String region;
		private List<DistrictDto> districts;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DistrictDto {
		private String name;
		private List<String> neighborhoods;
	}
}
