package com.hm.picplz.domain.member.dto.request;

import com.hm.picplz.domain.member.domain.Role;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateMemberRequest {

    private String nickname;
    private LocalDate birth;
    private Role role;
    private String socialEmail;
    private String profileImage;
}
