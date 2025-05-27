package com.hm.picplz.infra.s3;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class S3Dto {

	@Data
	@NoArgsConstructor
	public static class UploadUrlResponse {
		private String uploadUrl; // s3 presigned url
		private String objectKey; // DB에 저장할 object Key

		public static UploadUrlResponse of(String uploadUrl, String objectKey) {
			UploadUrlResponse response = new UploadUrlResponse();
			response.uploadUrl = uploadUrl;
			response.objectKey = objectKey;
			return response;
		}
	}
}
