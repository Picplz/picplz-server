package com.hm.picplz.domain.photographer.dto;

import java.util.List;

import com.hm.picplz.domain.photographer.domain.PhotoMood;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoMoodDto {

	@Data
	@NoArgsConstructor
	public static class AddPhotoMood {
		private List<String> photoMoods;

		public static AddPhotoMood of(List<String> photoMoods) {
			AddPhotoMood addPhotoMood = new AddPhotoMood();
			addPhotoMood.photoMoods = photoMoods;
			return addPhotoMood;
		}
	}

	@Data
	@NoArgsConstructor
	public static class PhotoMoodRes {
		private String photoMoodContent;

		public static PhotoMoodRes of(PhotoMood photoMood) {
			PhotoMoodRes photoMoodDto = new PhotoMoodRes();
			photoMoodDto.photoMoodContent = photoMood.getContent();
			return photoMoodDto;
		}
	}
}
