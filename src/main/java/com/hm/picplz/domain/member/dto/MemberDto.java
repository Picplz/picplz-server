package com.hm.picplz.domain.member.dto;

import java.time.LocalDate;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.domain.SocialProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDto {

	@Data
	@NoArgsConstructor
	public static class CreateMemberRequest {
		private String nickname;
		private String socialEmail;
		private Role role;
		private SocialProvider socialProvider;
		private String socialCode;
		private String profileImage;

		public static CreateMemberRequest of(MemberSignupInfo member, Role role) {
			CreateMemberRequest createMemberRequest = new CreateMemberRequest();
			createMemberRequest.nickname = member.getNickname();
			createMemberRequest.socialEmail = member.getSocialEmail();
			createMemberRequest.role = role;
			createMemberRequest.socialProvider = member.getSocialProvider();
			createMemberRequest.socialCode = member.getSocialCode();
			createMemberRequest.profileImage = member.getProfileImage();
			return createMemberRequest;
		}

	}


	@Data
	@NoArgsConstructor
	public static class UpdateMemberInfoRequest {
		@NotNull
		private Long id;
		private String nickname;
		private String profileImage;
		private String introduction;
		private String instagram;
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
		private Role role;
		private String socialEmail;
		private String profileImage;
		private SocialProvider socialProvider;
		private String socialCode;

		public static MemberInfoResponse from(Member member) {
			MemberInfoResponse memberInfoResponse = new MemberInfoResponse();
			memberInfoResponse.id = member.getId();
			memberInfoResponse.nickname = member.getNickname();
			memberInfoResponse.role = member.getRole();
			memberInfoResponse.socialEmail = member.getSocialEmail();
			memberInfoResponse.profileImage = member.getProfileImage();
			memberInfoResponse.socialProvider = member.getSocialProvider();
			memberInfoResponse.socialCode = member.getSocialCode();
			return memberInfoResponse;
		}
	}


	@Data
	@NoArgsConstructor
	public static class CreateMemberTest {
		private String nickname;
		private LocalDate birth;
		private Role role;
		private String socialEmail;
		private String profileImage;
	}
}
