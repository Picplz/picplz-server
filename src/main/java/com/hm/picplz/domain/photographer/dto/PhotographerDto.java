package com.hm.picplz.domain.photographer.dto;

import java.util.Comparator;
import java.util.List;

import com.hm.picplz.domain.member.domain.SocialProvider;
import com.hm.picplz.domain.member.dto.MemberSignupInfo;
import com.hm.picplz.domain.photographer.domain.ActiveArea;
import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.global.common.entity.YesNo;

import jakarta.validation.constraints.NotBlank;
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
        private List<ActiveAreaResponse> area;
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
                .map(ActiveAreaResponse::of)
                .sorted(Comparator.comparingInt(ActiveAreaResponse::getPriority))
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
    public static class ActiveAreaResponse {
        private Long code;
        private String name;
        private Integer priority;

        public static ActiveAreaResponse of(ActiveArea activeArea) {
            ActiveAreaResponse activeAreaResponse = new ActiveAreaResponse();
            activeAreaResponse.code     = activeArea.getArea().getId();
            activeAreaResponse.name     = activeArea.getName();
            activeAreaResponse.priority = activeArea.getPriority();
            return activeAreaResponse;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CreatePhotographerRequest implements MemberSignupInfo {
        @NotBlank
        private String nickname;
        private String socialEmail;
        private SocialProvider socialProvider;
        private String attributeCode;
        private String profileImage;

        private List<String> photoMoods;
        private List<ActiveAreaRequest> activeAreas;
        // TODO: 촬영 기기 추가
        // TODO: 자기소개, 인스타그램 작성할 수 있는 API 필요

        public static CreatePhotographerRequest of (String nickname, String socialEmail, SocialProvider socialProvider,
            String attributeCode, String profileImage, List<String> photoMoods, List<ActiveAreaRequest> activeAreas) {
            CreatePhotographerRequest createPhotographerRequest = new CreatePhotographerRequest();
            createPhotographerRequest.nickname = nickname;
            createPhotographerRequest.socialEmail = socialEmail;
            createPhotographerRequest.socialProvider = socialProvider;
            createPhotographerRequest.attributeCode = attributeCode;
            createPhotographerRequest.profileImage = profileImage;
            createPhotographerRequest.photoMoods = photoMoods;
            createPhotographerRequest.activeAreas = activeAreas;
            return createPhotographerRequest;
        }
    }

    @Data
    @NoArgsConstructor
    public static class ActiveAreaRequest {
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
