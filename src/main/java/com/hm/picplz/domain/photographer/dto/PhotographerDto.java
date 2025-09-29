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
        private long distance;
        private List<String> photoMoods;
        private Boolean isFollowing;
        private Boolean isOurPhotographer; // 우리 동네 작가

        public static Card of(Photographer photographer, long distance, Boolean isFollowing) {
            Card card = new Card();
            card.photographerId = photographer.getId();
            card.nickname = photographer.getMember().getNickname();
            card.profileImage = photographer.getMember().getProfileImage();
            card.active = photographer.getActive();
            card.distance = distance;
            card.photoMoods = photographer.getPhotoMoods().stream()
                    .map(PhotoMood::getContent)
                    .toList();
            card.isFollowing = isFollowing;
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
        private YesNo isFollowing;  // 팔로우 여부
        private int followers;
        private long distance;
        private YesNo isOurPhotographer;
        private List<String> phoneBrands; // 아이폰, 갤럭시
        private List<String> cameraTypes; // DSLR, 미러리스, 필름카메라, 디지털카메라
        //TODO: '별점순'을 위한 리뷰 별점 추가, '예약 많은 순'을 위한 최근 한달 예약 개수 추가

        public static Detail of(Photographer photographer, long distance, int followers, YesNo isFollowing, YesNo isOurPhotographer) {
            Detail detail = Detail.of(photographer);
            detail.distance = distance;
            detail.followers = followers;
            detail.isFollowing = isFollowing;
            detail.isOurPhotographer = isOurPhotographer;
            return detail;
        }

        public static Detail of(Photographer photographer, int followers, YesNo isFollowing) {
            Detail detail = Detail.of(photographer);
            detail.followers = followers;
            detail.isFollowing = isFollowing;
            detail.isOurPhotographer = YesNo.Y;
            return detail;
        }

        public static Detail of(Photographer photographer) {
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

            return detail;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CreatePhotographerRequest implements MemberSignupInfo {
        @NotBlank
        private String nickname;
        private String socialEmail;
        private SocialProvider socialProvider;  // 카카오 or 애플
        private String socialCode;
        private String profileImage;

        private List<String> photoMoods;
        private List<ActiveAreaRequest> activeAreas;
        private List<PhotographerCameraRequest> cameras;

        public static CreatePhotographerRequest of (String nickname, String socialEmail, SocialProvider socialProvider,
            String socialCode, String profileImage, List<String> photoMoods, List<ActiveAreaRequest> activeAreas,
            List<PhotographerCameraRequest> cameras) {
            CreatePhotographerRequest createPhotographerRequest = new CreatePhotographerRequest();
            createPhotographerRequest.nickname = nickname;
            createPhotographerRequest.socialEmail = socialEmail;
            createPhotographerRequest.socialProvider = socialProvider;
            createPhotographerRequest.socialCode = socialCode;
            createPhotographerRequest.profileImage = profileImage;
            createPhotographerRequest.photoMoods = photoMoods;
            createPhotographerRequest.activeAreas = activeAreas;
            createPhotographerRequest.cameras = cameras;
            return createPhotographerRequest;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CreatePhotographerResponse {
        private Long memberId;
        private Long photographerId;
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
        private String cameraType; // DSLR, 필름 등등...
    }

    @Data
    @NoArgsConstructor
    public static class UpdateActiveAreaRequest {
        List<ActiveAreaRequest> areas;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateActiveAreaResponse {
        List<ActiveAreaResponse> areas;

        public static UpdateActiveAreaResponse from(Photographer photographer) {
            UpdateActiveAreaResponse updateActiveAreaResponse = new UpdateActiveAreaResponse();
            updateActiveAreaResponse.areas = photographer.getActiveAreas()
                    .stream()
                    .map(ActiveAreaResponse::from)
                    .toList();
            return updateActiveAreaResponse;
        }
    }
}
