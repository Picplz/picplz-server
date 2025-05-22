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
                .map(ActiveAreaResponse::from)
                .sorted(Comparator.comparingInt(ActiveAreaResponse::getPriority))
                .toList();
            detail.active = photographer.getActive();
            detail.instagram = photographer.getMember().getInstagram();
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
    public static class CreatePhotographerRequest implements MemberSignupInfo {
        @NotBlank
        private String nickname;
        private String socialEmail;
        private SocialProvider socialProvider;
        private String attributeCode;
        private String profileImage;

        private List<String> photoMoods;
        private List<ActiveAreaRequest> activeAreas;
        private List<PhotographerCameraRequest> cameras;

        public static CreatePhotographerRequest of (String nickname, String socialEmail, SocialProvider socialProvider,
            String attributeCode, String profileImage, List<String> photoMoods, List<ActiveAreaRequest> activeAreas,
            List<PhotographerCameraRequest> cameras) {
            CreatePhotographerRequest createPhotographerRequest = new CreatePhotographerRequest();
            createPhotographerRequest.nickname = nickname;
            createPhotographerRequest.socialEmail = socialEmail;
            createPhotographerRequest.socialProvider = socialProvider;
            createPhotographerRequest.attributeCode = attributeCode;
            createPhotographerRequest.profileImage = profileImage;
            createPhotographerRequest.photoMoods = photoMoods;
            createPhotographerRequest.activeAreas = activeAreas;
            createPhotographerRequest.cameras = cameras;
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
    public static class ActiveAreaResponse {
        private Long code;
        private String name;
        private Integer priority;

        public static ActiveAreaResponse from(ActiveArea activeArea) {
            ActiveAreaResponse activeAreaResponse = new ActiveAreaResponse();
            activeAreaResponse.code     = activeArea.getArea().getId();
            activeAreaResponse.name     = activeArea.getArea().getName();
            activeAreaResponse.priority = activeArea.getPriority();
            return activeAreaResponse;
        }
    }

    @Data
    @NoArgsConstructor
    public static class PhotographerCameraRequest {
        private String type; // 핸드폰, 카메라
        private String brand; // 직접 입력 가능, 애플, 삼성, 소니
        private String name; // 직접 입력 가능, 모델명
        private String cameraBrand; // DSLR, 필름 등등...
    }
}
