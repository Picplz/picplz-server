package com.hm.picplz.infra.S3;

import java.util.UUID;

public enum ImageType {
    PROFILE("members/profile/"),    // 프로필 이미지 type
    PORTFOLIO("photographers/portfolio/");  // 포트폴리오 이미지 type

    private final String folder;

    ImageType(String folder) {
        this.folder = folder;
    }

    public String generateKeyWithUuid(String filename) {
        String uuid = UUID.randomUUID().toString();
        return folder + uuid + "/" + filename;
    }
}
