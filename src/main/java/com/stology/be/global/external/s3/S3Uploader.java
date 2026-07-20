package com.stology.be.global.external.s3;

import com.stology.be.global.external.s3.exception.S3ErrorCode;
import com.stology.be.global.external.s3.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.s3.path}")
    private String basePath;


    /**
     * MultipartFile을 S3에 업로드하고 객체 URL을 반환합니다.
     */
    public String uploadByFile(
            MultipartFile file,
            String dirName
    ) {
        validateFile(file);

        String originalFilename = getOriginalFilename(file);
        String contentType = getContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            return putS3(
                    inputStream,
                    originalFilename,
                    contentType,
                    file.getSize(),
                    dirName
            );
        } catch (IOException | SdkException e) {
            log.error(
                    "S3Uploader: file upload failed. originalFilename={}",
                    originalFilename,
                    e
            );

            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }
    }

    /**
     * 외부 URL의 파일을 읽어서 S3에 업로드합니다.
     */
    public String uploadByUrl(
            String fileUrl,
            String dirName
    ) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new S3Exception(S3ErrorCode.EMPTY_FILE);
        }

        try {
            URL url = URI.create(fileUrl).toURL();
            URLConnection connection = url.openConnection();

            String contentType =
                    getContentType(connection.getContentType());

            String originalFilename =
                    extractCleanFileName(fileUrl, contentType);

            /*
             * 일부 서버는 Content-Length를 전달하지 않아 -1이 나올 수 있습니다.
             * 이 경우 데이터를 byte[]로 읽어 업로드합니다.
             */
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] data = inputStream.readAllBytes();

                return putS3(
                        new java.io.ByteArrayInputStream(data),
                        originalFilename,
                        contentType,
                        data.length,
                        dirName
                );
            }
        } catch (Exception e) {
            log.error(
                    "S3Uploader: URL upload failed. url={}",
                    fileUrl,
                    e
            );

            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }
    }

    /**
     * byte 배열을 S3에 업로드합니다.
     */
    public String uploadBytes(
            byte[] data,
            String fileName,
            String contentType,
            String dirName
    ) {
        if (data == null || data.length == 0) {
            throw new S3Exception(S3ErrorCode.EMPTY_FILE);
        }

        String safeContentType = getContentType(contentType);
        String objectKey = createObjectKey(dirName, fileName);

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(safeContentType)
                    .contentLength((long) data.length)
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(data)
            );

            log.info(
                    "S3Uploader: byte upload successful. bucket={}, key={}",
                    bucket,
                    objectKey
            );

            return createObjectUrl(objectKey);

        } catch (SdkException e) {
            log.error(
                    "S3Uploader: byte upload failed. bucket={}, key={}",
                    bucket,
                    objectKey,
                    e
            );

            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }
    }

    /**
     * S3 업로드 공통 로직입니다.
     */
    private String putS3(
            InputStream inputStream,
            String originalFilename,
            String contentType,
            long contentLength,
            String dirName
    ) {
        String objectKey =
                createObjectKey(dirName, originalFilename);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        inputStream,
                        contentLength
                )
        );

        log.info(
                "S3Uploader: upload successful. bucket={}, key={}",
                bucket,
                objectKey
        );

        return createObjectUrl(objectKey);
    }

    /**
     * S3 객체 키를 생성합니다.
     *
     * 예:
     * study-material/UUID_파일명.md
     */
    private String createObjectKey(
            String dirName,
            String originalFilename
    ) {
        String safeDirectory =
                normalizeDirectory(dirName);

        String safeFileName =
                encodeFileName(originalFilename);

        return basePath
                + "/"
                + safeDirectory
                + "/"
                + UUID.randomUUID()
                + "_"
                + safeFileName;
    }

    private String createObjectUrl(String objectKey) {
        URL url = s3Client.utilities()
                .getUrl(builder -> builder
                        .bucket(bucket)
                        .key(objectKey)
                );

        return url.toString();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new S3Exception(S3ErrorCode.EMPTY_FILE);
        }
    }

    private String getOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            return "file";
        }

        /*
         * 브라우저 또는 클라이언트가 전체 경로를 보내는 경우
         * 마지막 파일명만 사용합니다.
         */
        String normalized = originalFilename.replace("\\", "/");

        return normalized.substring(
                normalized.lastIndexOf("/") + 1
        );
    }

    private String encodeFileName(String filename) {
        return URLEncoder.encode(
                        filename,
                        StandardCharsets.UTF_8
                )
                .replace("+", "%20");
    }

    private String normalizeDirectory(String dirName) {
        if (dirName == null || dirName.isBlank()) {
            return "files";
        }

        String normalized = dirName
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");

        if (normalized.contains("..")) {
            throw new S3Exception(S3ErrorCode.S3_UPLOAD_FAIL);
        }

        return normalized;
    }

    private String getContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }

        return contentType;
    }

    /**
     * URL 경로에서 파일명을 추출합니다.
     */
    private String extractCleanFileName(
            String fileUrl,
            String contentType
    ) {
        String path = URI.create(fileUrl).getPath();

        if (path == null || path.isBlank()) {
            return createDefaultFilename(contentType);
        }

        String fileName =
                path.substring(path.lastIndexOf("/") + 1);

        if (fileName.isBlank()
                || fileName.length() > 100
                || !fileName.contains(".")) {
            return createDefaultFilename(contentType);
        }

        return fileName;
    }

    private String createDefaultFilename(String contentType) {
        String extension = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "text/markdown" -> ".md";
            case "application/pdf" -> ".pdf";
            default -> ".bin";
        };

        return "file" + extension;
    }
}