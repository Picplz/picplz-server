package com.hm.picplz.infra.s3;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/s3")
@Tag(name = "S3")
public class S3PresignedUrlController {

    private final S3PresignedUrlService s3PresignedUrlService;

    @Operation(summary = "다운로드용 presignedURL 발급", description = "조회는 cloud front가 편할 것 같아서 "
        + "cloud front 추가해두었습니다. 링크는 `d3gsimgp4t78ys.cloudfront.net` 입니다. "
        + "하지만 혹시 몰라서 download url api도 남겨둡니다.")
    @GetMapping("/presigned-download-url")
    public S3Dto.DownloadUrlResponse getDownloadUrl(@RequestParam("objectKey") String objectKey) {
        URL url = s3PresignedUrlService.generateDownloadUrl(objectKey);
        return S3Dto.DownloadUrlResponse.of(url.toString());
    }

    @Operation(summary = "업로드용 presignedURL 발급", description = "응답값의 objectKey를 프로필 이미지 수정 / 포트폴리오 이미지 등록 요청 값에 포함해주시면 됩니다.")
    @GetMapping("/presigned-upload-url")
    public S3Dto.UploadUrlResponse getUploadUrl(
            @RequestParam("imageType") ImageType imageType,
            @RequestParam("filename") String filename
    ) {
        return s3PresignedUrlService.generateUploadUrl(imageType, filename);
    }
}
