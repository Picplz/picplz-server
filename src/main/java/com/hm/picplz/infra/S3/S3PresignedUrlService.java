package com.hm.picplz.infra.S3;

import java.net.URL;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(PRESIGNED_DOWNLOAD_DURATION)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url();
    }

    /**
     * 업로드 presignedURL 생성
     * @param type  업로드 타입 (PROFILE/PORTFOLIO)
     * @param filename 파일 명
     * @return 미리 생성된 url
     */
    public URL generateUploadUrl(ImageType type, String filename) {
        String objectKey = type.generateKeyWithUuid(filename);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType("image/jpeg") // 필요 시 확장
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(PRESIGNED_UPLOAD_DURATION)
                .build();

        return s3Presigner.presignPutObject(presignRequest).url();
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
