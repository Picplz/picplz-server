package com.hm.picplz.domain.following.domain;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Following extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "following_id", updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "following_user_id", nullable = false)
    private Member following; // 팔로우 당하는 사람

    @ManyToOne
    @JoinColumn(name = "follower_user_id", nullable = false)
    private Member follower; // 팔로우 하는 사람

    @Builder
    public Following(Member following, Member follower) {
        this.following = following;
        this.follower = follower;
    }

    public static Following of(Member following, Member follower) {
        return Following.builder()
                .following(following)
                .follower(follower)
                .build();
    }
}
