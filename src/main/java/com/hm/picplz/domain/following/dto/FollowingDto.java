package com.hm.picplz.domain.following.dto;

import java.util.Comparator;
import java.util.List;

import com.hm.picplz.domain.photographer.domain.ActiveArea;
import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.global.common.entity.YesNo;

import lombok.Data;
import lombok.NoArgsConstructor;

public class FollowingDto {

    @Data
    @NoArgsConstructor
    public static class PhotographerCard {
        private Long photographerId;
        private String nickname;
        private String profileImage;
        private YesNo active;
        private List<String> photoMoods;
        private List<ActiveAreaInfo> areas;

        public static PhotographerCard from(Photographer photographer) {
            PhotographerCard card = new PhotographerCard();
            card.photographerId = photographer.getId();
            card.nickname = photographer.getMember().getNickname();
            card.profileImage = photographer.getMember().getProfileImage();
            card.active = photographer.getActive();
            card.photoMoods = photographer.getPhotoMoods().stream()
                    .map(PhotoMood::getContent)
                    .toList();
            card.areas = photographer.getActiveAreas().stream()
                    .sorted(Comparator.comparingInt(ActiveArea::getPriority))
                    .map(ActiveAreaInfo::from)
                    .toList();
            return card;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ActiveAreaInfo {
        private Long code;
        private String name;
        private String sigungu;

        public static ActiveAreaInfo from(ActiveArea activeArea) {
            ActiveAreaInfo info = new ActiveAreaInfo();
            info.code = activeArea.getArea().getId();
            info.name = activeArea.getArea().getName();
            info.sigungu = activeArea.getArea().getSigungu();
            return info;
        }
    }

    @Data
    @NoArgsConstructor
    public static class IsFollowingResponse {
        private boolean following;

        public static IsFollowingResponse of(boolean following) {
            IsFollowingResponse response = new IsFollowingResponse();
            response.following = following;
            return response;
        }
    }
}
