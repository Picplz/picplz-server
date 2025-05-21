package com.hm.picplz.domain.photographer.dto;

import java.util.List;

import com.hm.picplz.domain.photographer.domain.PhotoMood;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoMoodDto {

	@Data
	@NoArgsConstructor
	public static class PhotoMoodRequest {
		@NotBlank
		private String photoMood;
	}

	@Data
	@NoArgsConstructor
	public static class PhotoMoodResponse {
		private List<String> photoMoods;

		public static PhotoMoodResponse of(List<PhotoMood> photoMoods) {
			PhotoMoodResponse photoMoodDto = new PhotoMoodResponse();
			photoMoodDto.photoMoods = photoMoods.stream().map(PhotoMood::getContent).toList();
			return photoMoodDto;
		}
	}
}
