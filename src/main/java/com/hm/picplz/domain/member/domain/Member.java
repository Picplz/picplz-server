package com.hm.picplz.domain.member.domain;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", updatable = false)
    private Long id;

    @NotNull
    @Size(max = 30)
    @Column(unique = true)
    private String nickname;

    // 삭제해야할 것 같습니다.
    // 회원가입시 입력받지 않으며, 카카오 회원정보에서 확인해도 기획상 연령대를 사용하는 부분도 없습니다.
    // 하지만 추후 기획 변경이 될 수도 있으니 당장 삭제하지는 않겠습니다.
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 고객 or 작가

    // 추후 사업자 인증을 받는다면 빈값이 되면 안될 것 같습니다.
    private String socialEmail;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider socialProvider; // 카카오 or 애플

    @NotNull
    private String socialCode;

    private String instagram; // 인스타그램 아이디

    private String introduction; // 소갯말

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Photographer photographer;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Customer customer;

    @Builder
    private Member(Long id, String nickname, LocalDate birth, Role role, String socialEmail, String profileImage,
        SocialProvider socialProvider, String socialCode) {
        this.id = id;
        this.nickname = nickname;
        this.birth = birth;
        this.role = role;
        this.socialEmail = socialEmail;
        this.profileImage = profileImage;
        this.socialProvider = socialProvider;
        this.socialCode = socialCode;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }

    public void updateNickname(String nickname) {
        if (StringUtils.hasText(nickname)) this.nickname = nickname;
    }

    public void updateProfileImage(String profileImage) {
        if (StringUtils.hasText(profileImage)) this.profileImage = profileImage;
    }

    public void updateInstagram(String instagram) { if (StringUtils.hasText(instagram)) this.instagram = instagram; }

    public void updateIntroduction(String introduction) {
        if (StringUtils.hasText(introduction)) this.introduction = introduction; }
}
