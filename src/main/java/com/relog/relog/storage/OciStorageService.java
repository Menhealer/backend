package com.relog.relog.storage;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.relog.relog.storage.exception.InvalidFileException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OciStorageService {

    private final ObjectStorageClient objectStorageClient;

    @Value("${oci.namespace}")
    private String namespace;

    @Value("${oci.bucket}")
    private String bucket;

    @Value("${oci.region:ap-chuncheon-1}")
    private String region;

    private static final String PROFILE_IMAGE_DIR = "profile-images/";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public String uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        validateImageFile(file);
        String fileName = generateFileName(memberId, file.getOriginalFilename());
        String objectName = PROFILE_IMAGE_DIR + fileName;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucket)
                .objectName(objectName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .putObjectBody(new ByteArrayInputStream(file.getBytes()))
                .build();

        objectStorageClient.putObject(putRequest);

        return createParUrl(objectName);
    }

    public void deleteProfileImage(String imageUrl) {
        String objectName = extractObjectNameFromParUrl(imageUrl);
        if (objectName == null) {
            return;
        }

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucket)
                .objectName(objectName)
                .build();

        objectStorageClient.deleteObject(deleteRequest);
    }

    private String createParUrl(String objectName) {
        Date expiration = Date.from(Instant.now().plus(3650, ChronoUnit.DAYS));

        CreatePreauthenticatedRequestDetails parDetails = CreatePreauthenticatedRequestDetails.builder()
                .name("par-" + objectName + "-" + UUID.randomUUID())
                .objectName(objectName)
                .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
                .timeExpires(expiration)
                .build();

        CreatePreauthenticatedRequestRequest parRequest = CreatePreauthenticatedRequestRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucket)
                .createPreauthenticatedRequestDetails(parDetails)
                .build();

        CreatePreauthenticatedRequestResponse response = objectStorageClient.createPreauthenticatedRequest(parRequest);

        String accessUri = response.getPreauthenticatedRequest().getAccessUri();
        return String.format("https://objectstorage.%s.oraclecloud.com%s", region, accessUri);
    }

    private String extractObjectNameFromParUrl(String url) {
        if (url == null) {
            return null;
        }
        // PAR URL format: https://objectstorage.<region>.oraclecloud.com/p/<par-id>/n/<ns>/b/<bucket>/o/<objectName>
        String marker = "/o/";
        int index = url.lastIndexOf(marker);
        if (index == -1) {
            return null;
        }
        return url.substring(index + marker.length());
    }

    private void validateImageFile(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, webp만 가능)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("허용되지 않는 파일 타입입니다.");
        }
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
}
