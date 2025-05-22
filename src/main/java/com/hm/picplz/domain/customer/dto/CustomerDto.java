package com.hm.picplz.domain.customer.dto;

import com.hm.picplz.domain.member.domain.SocialProvider;
import com.hm.picplz.domain.member.dto.MemberSignupInfo;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerDto {

	@Data
	@NoArgsConstructor
	public static class CreateCustomerRequest implements MemberSignupInfo {
		@NotBlank
		private String nickname;
		private String socialEmail;
		private SocialProvider socialProvider;
		private String attributeCode;
		private String profileImage;
	}
}
