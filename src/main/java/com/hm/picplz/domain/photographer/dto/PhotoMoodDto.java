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
	public static class PhotoMoodReq {
		@NotBlank
		private String photoMood;
	}

	@Data
	@NoArgsConstructor
	public static class PhotoMoodRes {
		private List<String> photoMoods;

		public static PhotoMoodRes of(List<PhotoMood> photoMoods) {
			PhotoMoodRes photoMoodDto = new PhotoMoodRes();
			photoMoodDto.photoMoods = photoMoods.stream().map(PhotoMood::getContent).toList();
			return photoMoodDto;
		}
	}
}
