package com.relog.relog.storage;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnBean(ObjectStorage.class)
public class OciStorageService {

    private final ObjectStorage objectStorageClient;
    private final String namespace;
    private final String bucketName;
    private final String region;

    public OciStorageService(
            ObjectStorage objectStorageClient,
            @Value("${oci.namespace}") String namespace,
            @Value("${oci.bucket}") String bucketName,
            @Value("${oci.region}") String region) {
        this.objectStorageClient = objectStorageClient;
        this.namespace = namespace;
        this.bucketName = bucketName;
        this.region = region;
    }

    public String uploadProfileImage(Long memberId, MultipartFile file) throws IOException {
        String fileName = generateFileName(memberId, file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(fileName)
                    .contentLength(file.getSize())
                    .contentType(file.getContentType())
                    .putObjectBody(inputStream)
                    .build();

            objectStorageClient.putObject(request);
        }

        return buildObjectUrl(fileName);
    }

    public void deleteProfileImage(String imageUrl) {
        String objectName = extractObjectName(imageUrl);

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .namespaceName(namespace)
                .bucketName(bucketName)
                .objectName(objectName)
                .build();

        objectStorageClient.deleteObject(request);
    }

    private String generateFileName(Long memberId, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return "profile/" + memberId + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String buildObjectUrl(String objectName) {
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                region, namespace, bucketName, objectName);
    }

    private String extractObjectName(String imageUrl) {
        String prefix = String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/",
                region, namespace, bucketName);
        return imageUrl.replace(prefix, "");
    }
}
