package com.hm.picplz.domain.photographer.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.global.common.entity.YesNo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotographerDto {

    @Data
    @NoArgsConstructor
    public static class Card {

        private Long photographerId;
        private String nickname;
        private String profileImage;
        private YesNo active;
        private double distance;
        private List<String> photoMoods;

        public static Card of(Photographer photographer, double distance) {
            Card card = new Card();
            card.photographerId = photographer.getId();
            card.nickname = photographer.getMember().getNickname();
            card.profileImage = photographer.getMember().getProfileImage();
            card.active = photographer.getActive();
            card.distance = distance;
            card.photoMoods = photographer.getPhotoMoods().stream()
                    .map(PhotoMood::getContent)
                    .collect(Collectors.toList());

            return card;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Detail {

        private Long photographerId;
        private String nickname;
        private String profileImage;
        private String area;
        private String introduction;
        private YesNo active;
        private String instagram;
        private List<String> photoMoods;
        private int followers;
        private YesNo isFollowing;  // 팔로우 여부

        public static Detail of(Photographer photographer, int followers, YesNo isFollowing) {
            Detail detail = new Detail();
            detail.photographerId = photographer.getId();
            detail.nickname = photographer.getMember().getNickname();
            detail.profileImage = photographer.getMember().getProfileImage();
            detail.area = photographer.getArea();
            detail.active = photographer.getActive();
            detail.instagram = photographer.getInstagram();
            detail.photoMoods = photographer.getPhotoMoods().stream()
                    .map(PhotoMood::getContent)
                    .toList();
            detail.followers = followers;
            detail.isFollowing = isFollowing;

            return detail;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Create {
        private String area;

        private int period;

        private String instagram;

        private String introduction;

        private List<String> photoMoods;

        public static Create of (String area, int period, String instagram, String introduction, List<String> photoMoods) {
            Create create = new Create();
            create.area = area;
            create.period = period;
            create.instagram = instagram;
            create.introduction = introduction;
            create.photoMoods = photoMoods;

            return create;
        }
    }
}
