package com.stology.be.global.external.s3;

import com.stology.be.global.external.s3.exception.S3ErrorCode;
import com.stology.be.global.external.s3.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Reader {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3 객체를 byte[]로 읽습니다.
     */
    public byte[] readBytes(String objectKey) {
        validateObjectKey(objectKey);

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();

            ResponseBytes<GetObjectResponse> response =
                    s3Client.getObject(
                            request,
                            ResponseTransformer.toBytes()
                    );

            byte[] data = response.asByteArray();

            log.info(
                    "S3Reader: object read successful. bucket={}, key={}, size={}",
                    bucket,
                    objectKey,
                    data.length
            );

            return data;

        } catch (NoSuchKeyException exception) {
            log.warn(
                    "S3Reader: object not found. bucket={}, key={}",
                    bucket,
                    objectKey
            );

            throw new S3Exception(
                    S3ErrorCode.EMPTY_FILE
            );

        } catch (software.amazon.awssdk.services.s3.model.S3Exception exception) {
            log.error(
                    "S3Reader: object read failed. bucket={}, key={}, statusCode={}",
                    bucket,
                    objectKey,
                    exception.statusCode(),
                    exception
            );

            throw new S3Exception(
                    S3ErrorCode.S3_READ_FAIL
            );
        }
    }

    /**
     * S3 객체를 UTF-8 문자열로 읽습니다.
     */
    public String readString(String objectKey) {
        return readString(
                objectKey,
                StandardCharsets.UTF_8
        );
    }

    /**
     * 지정한 인코딩으로 S3 객체를 문자열로 읽습니다.
     */
    public String readString(
            String objectKey,
            Charset charset
    ) {
        Charset safeCharset =
                charset == null
                        ? StandardCharsets.UTF_8
                        : charset;

        return new String(
                readBytes(objectKey),
                safeCharset
        );
    }

    private void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new S3Exception(
                    S3ErrorCode.S3_NO_OBJECT_KEY
            );
        }
    }
}