package com.hm.picplz.domain.photographer.dto;

import com.hm.picplz.domain.photographer.domain.DefaultCamera;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultCameraDto {

	@Data
	@NoArgsConstructor
	public static class Card {
		private String type; // 핸드폰, 카메라
		private String brand; // 애플, 삼성, 소니 ...
		private String name; // 모델명

		public static Card from(DefaultCamera defaultCamera) {
			Card card = new Card();
			card.type = defaultCamera.getType();
			card.name = defaultCamera.getName();
			card.brand = defaultCamera.getBrand();
			return card;
		}
	}
}
