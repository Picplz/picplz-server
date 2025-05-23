package com.hm.picplz.domain.photographer.dto;

import com.hm.picplz.domain.photographer.domain.DefaultCamera;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CameraDto {

	@Data
	@NoArgsConstructor
	public static class DefaultCameraCard {
		private String type; // 핸드폰, 카메라
		private String brand; // 애플, 삼성, 소니 ...
		private String name; // 모델명

		public static DefaultCameraCard from(DefaultCamera defaultCamera) {
			DefaultCameraCard defaultCameraCard = new DefaultCameraCard();
			defaultCameraCard.type = defaultCamera.getType();
			defaultCameraCard.name = defaultCamera.getName();
			defaultCameraCard.brand = defaultCamera.getBrand();
			return defaultCameraCard;
		}
	}
}
