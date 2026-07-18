package com.hm.picplz.domain.photographer.dto;

import java.util.List;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;

import com.hm.picplz.global.common.entity.YesNo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(name = "PhotographerSearchResponse")
public class PhotographerSearchDto {

    private Long photographerId;
    private String nickname;
    private String profileImage;
    private YesNo isActive;
    private List<String> photoMoods;

    public static PhotographerSearchDto of(Photographer photographer) {
        PhotographerSearchDto dto = new PhotographerSearchDto();

        dto.photographerId = photographer.getId();
        dto.nickname = photographer.getMember().getNickname();
        dto.profileImage = photographer.getMember().getProfileImage();
        dto.isActive = photographer.getActive();
        dto.photoMoods = photographer.getPhotoMoods().stream()
                .map(PhotoMood::getContent)
                .toList();

        return dto;
    }
}