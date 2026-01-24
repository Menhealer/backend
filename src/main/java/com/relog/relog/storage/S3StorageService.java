package com.relog.relog.storage;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private static final String PROFILE_IMAGE_DIR = "profile-images/";

    public String uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        String fileName = generateFileName(memberId, file.getOriginalFilename());
        String key = PROFILE_IMAGE_DIR + fileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        return getFileUrl(key);
    }

    public void deleteProfileImage(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        if (key == null) {
            return;
        }

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
    }

    private String generateFileName(Long memberId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return memberId + "_" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    private String getFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    private String extractKeyFromUrl(String url) {
        if (url == null || !url.contains(bucket)) {
            return null;
        }
        int keyStart = url.indexOf(bucket) + bucket.length() + 1;
        if (keyStart >= url.length()) {
            return null;
        }
        return url.substring(keyStart);
    }
}
