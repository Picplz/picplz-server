package com.hm.picplz.infra.S3;

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

    @Operation(summary = "다운로드용 presignedURL 발급")
    @GetMapping("/presigned-download-url")
    public String getDownloadUrl(@RequestParam("objectKey") String objectKey) {
        URL url = s3PresignedUrlService.generateDownloadUrl(objectKey);
        return url.toString();
    }

    @Operation(summary = "업로드용 presignedURL 발급")
    @GetMapping("/presigned-upload-url")
    public String getUploadUrl(
            @RequestParam("imageType") ImageType imageType,
            @RequestParam("filename") String filename
    ) {
        URL url = s3PresignedUrlService.generateUploadUrl(imageType, filename);
        return url.toString();
    }
}
