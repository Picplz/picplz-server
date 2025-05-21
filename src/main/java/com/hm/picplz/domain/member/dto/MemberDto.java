package com.hm.picplz.domain.member.dto;

import java.time.LocalDate;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDto {

	@Data
	@NoArgsConstructor
	public static class CreateMemberTestRequest {
		private String nickname;
		private LocalDate birth;
		private Role role;
		private String socialEmail;
		private String profileImage;
	}

	@Data
	@NoArgsConstructor
	public static class UpdateMemberInfoRequest {
		private Long id;
		@NotNull
		@Size(max = 30)
		private String nickname;
		private String profileImage;
	}

	@Data
	@NoArgsConstructor
	public static class UpdateMemberLocationRequest {
		@Schema(defaultValue = "1", description = "회원 아이디")
		@NotNull(message = "회원 아이디를 입력해주세요")
		private Long memberId;

		@Schema(defaultValue = "37.5665", description = "위도")
		@NotNull(message = "위도를 입력해주세요")
		private double latitude;

		@Schema(defaultValue = "126.9780", description = "경도")
		@NotNull(message = "경도를 입력해주세요")
		private double longitude;
	}

	@Data
	@NoArgsConstructor
	public static class MemberInfoResponse {
		private Long id;
		private String nickname;
		private LocalDate birth;
		private Role role;
		private String socialEmail;
		private String profileImage;
		private String provider;
		private String attributeCode;

		@Builder
		public static MemberInfoResponse of(Member member) {
			MemberInfoResponse memberInfoResponse = new MemberInfoResponse();
			memberInfoResponse.id = member.getId();
			memberInfoResponse.nickname = member.getNickname();
			memberInfoResponse.birth = member.getBirth();
			memberInfoResponse.role = member.getRole();
			memberInfoResponse.socialEmail = member.getSocialEmail();
			memberInfoResponse.profileImage = member.getProfileImage();
			memberInfoResponse.provider = member.getProvider();
			memberInfoResponse.attributeCode = member.getAttributeCode();
			return memberInfoResponse;
		}
	}
}
