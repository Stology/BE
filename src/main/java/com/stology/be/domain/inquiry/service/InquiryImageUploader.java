package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InquiryImageUploader {

    private final S3Template s3Template;
    private final S3Presigner s3Presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.s3.path:}")
    private String pathPrefix;

    public List<String> uploadAll(List<MultipartFile> files, String subDirectory) {
        return files.stream()
                .map(file -> upload(file, subDirectory))
                .collect(Collectors.toList());
    }

    public String upload(MultipartFile file, String subDirectory) {
        if (file.isEmpty() || file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new InquiryException(InquiryErrorCode.IMAGE_FILE_INVALID);
        }

        String key = pathPrefix + subDirectory + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try (var inputStream = file.getInputStream()) {
            S3Resource resource = s3Template.upload(bucket, key, inputStream);
            return resource.getURL().toString();
        } catch (IOException e) {
            throw new InquiryException(InquiryErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    /**
     * 버킷 객체가 비공개(Block Public Access)라 image_url을 그대로 내려주면 브라우저에서
     * 403이 난다. 응답 시점에 같은 key로 짧은 만료의 presigned GET URL을 새로 발급해 내려준다.
     */
    public String toDisplayUrl(String storedUrl) {
        String key = extractKey(storedUrl);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * 선업로드 API가 내려준 URL을 클라이언트가 되돌려주면 DB에 저장하기 전에 검증한다.
     * 임의의 외부 URL이 image_url로 저장되는 것을 막고, presigned 쿼리스트링이 붙어 돌아온
     * 경우에도 만료 정보 없는 정규 형태(scheme://host/key)로 normalize 한다.
     */
    public String toStoredUrl(String uploadedUrl) {
        if (uploadedUrl == null || uploadedUrl.isBlank()) {
            throw new InquiryException(InquiryErrorCode.IMAGE_URL_INVALID);
        }

        URI uri;
        try {
            uri = URI.create(uploadedUrl);
        } catch (IllegalArgumentException e) {
            throw new InquiryException(InquiryErrorCode.IMAGE_URL_INVALID);
        }

        String host = uri.getHost();
        if (host == null || !host.contains(bucket)) {
            throw new InquiryException(InquiryErrorCode.IMAGE_URL_INVALID);
        }

        String key = extractKey(uploadedUrl);
        if (key.isBlank() || !key.startsWith(pathPrefix)) {
            throw new InquiryException(InquiryErrorCode.IMAGE_URL_INVALID);
        }

        return uri.getScheme() + "://" + host + "/" + key;
    }

    private String extractKey(String storedUrl) {
        String path = URI.create(storedUrl).getPath();
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
