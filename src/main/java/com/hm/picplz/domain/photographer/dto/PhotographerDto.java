package com.hm.picplz.domain.photographer.dto;

import java.util.Comparator;
import java.util.List;

import com.hm.picplz.domain.photographer.domain.ActiveArea;
import com.hm.picplz.domain.photographer.domain.CareerType;
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
                    .toList();

            return card;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Detail {

        private Long photographerId;
        private String nickname;
        private String profileImage;
        private List<ActiveAreaRes> area;
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
            detail.area = photographer.getActiveAreas().stream()
                .map(ActiveAreaRes::of)
                .sorted(Comparator.comparingInt(ActiveAreaRes::getPriority))
                .toList();
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
    public static class ActiveAreaRes {
        private Long code;
        private String name;
        private Integer priority;

        public static ActiveAreaRes of(ActiveArea activeArea) {
            ActiveAreaRes activeAreaRes = new ActiveAreaRes();
            activeAreaRes.code     = activeArea.getArea().getId();
            activeAreaRes.name     = activeArea.getName();
            activeAreaRes.priority = activeArea.getPriority();
            return activeAreaRes;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Create {
        private int year;
        private int month;
        private String instagram;
        private String introduction;
        private List<String> photoMoods;
        private List<ActiveAreaReq> activeAreas;

        public static Create of (int year, int month, String instagram, String introduction, List<String> photoMoods,
            List<ActiveAreaReq> activeAreas) {
            Create create = new Create();
            create.year = year;
            create.month = month;
            create.instagram = instagram;
            create.introduction = introduction;
            create.photoMoods = photoMoods;
            create.activeAreas = activeAreas;

            return create;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ActiveAreaReq {
        private Long code;
        private Integer priority;
    }

    @Data
    @NoArgsConstructor
    public static class AddCareer {
        private List<CareerType> careers;

        public static AddCareer of(List<CareerType> careers) {
            AddCareer addCareer = new AddCareer();
            addCareer.careers = careers;
            return addCareer;
        }
    }

    @Data
    @NoArgsConstructor
    public static class UpdateCareerPeriod {
        private int year;
        private int month;
        public static UpdateCareerPeriod of(int year, int month) {
            UpdateCareerPeriod updateCareerPeriod = new UpdateCareerPeriod();
            updateCareerPeriod.year = year;
            updateCareerPeriod.month = month;
            return updateCareerPeriod;
        }
    }
}
