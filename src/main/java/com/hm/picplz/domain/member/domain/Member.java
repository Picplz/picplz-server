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

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String socialEmail;

    private String profileImage;

    private String provider;

    private String attributeCode;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Photographer photographer;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Customer customer;

    @Builder
    private Member(Long id, String nickname, LocalDate birth, Role role, String socialEmail, String profileImage, String provider, String attributeCode) {
        this.id = id;
        this.nickname = nickname;
        this.birth = birth;
        this.role = role;
        this.socialEmail = socialEmail;
        this.profileImage = profileImage;
        this.provider = provider;
        this.attributeCode = attributeCode;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
    // factory method
}
