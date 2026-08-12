package com.stology.be.global.external.s3;

import com.stology.be.global.external.s3.exception.S3ErrorCode;
import com.stology.be.global.external.s3.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Remover {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 저장된 파일을 삭제합니다.
     *
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    public void remove(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.info("S3Remover: skipped because URL is empty.");
            return;
        }

        String objectKey = extractObjectKey(fileUrl);

        try {
            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build();

            s3Client.deleteObject(request);

            log.info(
                    "S3Remover: delete successful. bucket={}, key={}",
                    bucket,
                    objectKey
            );

        } catch (SdkException e) {
            log.error(
                    "S3Remover: delete failed. bucket={}, key={}",
                    bucket,
                    objectKey,
                    e
            );

            throw new S3Exception(S3ErrorCode.S3_REMOVE_FAIL);
        }
    }

    private String extractObjectKey(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            String rawPath = uri.getRawPath();

            if (rawPath == null || rawPath.isBlank()
                    || rawPath.equals("/")) {
                log.warn(
                        "S3Remover: invalid URL path. url={}",
                        fileUrl
                );

                throw new S3Exception(
                        S3ErrorCode.S3_REMOVE_FAIL
                );
            }

            String encodedKey =
                    rawPath.startsWith("/")
                            ? rawPath.substring(1)
                            : rawPath;

            /*
             * 업로드 시 파일명을 URL 인코딩했으므로
             * S3 삭제 요청에는 다시 원래 키로 디코딩합니다.
             */
            return URLDecoder.decode(
                    encodedKey,
                    StandardCharsets.UTF_8
            );

        } catch (S3Exception e) {
            throw e;

        } catch (Exception e) {
            log.error(
                    "S3Remover: URL parsing failed. url={}",
                    fileUrl,
                    e
            );

            throw new S3Exception(
                    S3ErrorCode.S3_REMOVE_FAIL
            );
        }
    }
}