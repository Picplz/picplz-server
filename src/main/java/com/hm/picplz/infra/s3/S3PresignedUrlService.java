package com.hm.picplz.infra.s3;

import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    public static final Duration PRESIGNED_UPLOAD_DURATION = Duration.ofMinutes(10);
    public static final Duration PRESIGNED_DOWNLOAD_DURATION = Duration.ofMinutes(5);

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 다운로드 presignedURL 생성
     * @param objectKey S3 객체 키 (예: "members/profile/87e35a17-1234-4c5c-8ef9-abc123/profile.jpg")
     * @return Presigned URL
     */
    public URL generateDownloadUrl(String objectKey) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(request -> request.bucket(bucketName).key(objectKey))
                .signatureDuration(PRESIGNED_DOWNLOAD_DURATION)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    /**
     * 업로드 presignedURL 생성
     * @param type  업로드 타입 (PROFILE/PORTFOLIO)
     * @param filename 파일 명
     * @return 미리 생성된 url + 해당 파일 Object Key
     */
    public S3Dto.UploadUrlResponse generateUploadUrl(ImageType type, String filename) {
        String objectKey = type.generateKeyWithUuid(filename);
        String contentType = resolveContentType(filename);

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .putObjectRequest(request -> request
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType))
            .signatureDuration(PRESIGNED_UPLOAD_DURATION)
            .build();

        return S3Dto.UploadUrlResponse.of(s3Presigner.presignPutObject(presignRequest).url().toString(), objectKey);
    }

    private String resolveContentType(String filename) {
        String extension = Optional.ofNullable(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(filename.lastIndexOf('.') + 1))
            .orElse("")
            .toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> throw ExceptionFactory.of(S3ErrorCode.WRONG_IMAGE_FILE_TYPE);
        };
    }

    /**
     * 사용하지 않는 S3 Object 삭제
     * @param objectKey 삭제할 ObjectKey (db에서 가져옴)
     */
    public void deleteS3Object(String objectKey) {
        s3Client.deleteObject(builder -> builder
                .bucket(bucketName)
                .key(objectKey)
        );
    }
}
