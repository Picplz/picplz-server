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
	public static class Card {
		private Long id;
		private String name;
		private String dong;
		private String ri;

		public static Card from(Area area) {
			Card card = new Card();
			card.id = area.getId();
			card.name = area.getName();
			card.dong = area.getEupmyeondong();
			card.ri = area.getRi();
			return card;
		}
	}
}
