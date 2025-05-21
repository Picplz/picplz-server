package com.hm.picplz.domain.member.dto.response;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MemberInfoResponse {

    private Long id;
    private String nickname;
    private LocalDate birth;
    private Role role;
    private String socialEmail;
    private String profileImage;
    private String provider;
    private String attributeCode;

    @Builder
    public MemberInfoResponse(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.birth = member.getBirth();
        this.role = member.getRole();
        this.socialEmail = member.getSocialEmail();
        this.profileImage = member.getProfileImage();
        this.provider = member.getProvider();
        this.attributeCode = member.getAttributeCode();
    }
}
