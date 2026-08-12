package com.stology.be.domain.inquiry.service;

import com.stology.be.domain.inquiry.exception.InquiryErrorCode;
import com.stology.be.domain.inquiry.exception.InquiryException;
import com.stology.be.global.external.s3.S3Remover;
import com.stology.be.global.external.s3.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * inquiry 이미지의 S3 업로드/삭제와 파일 검증(매직 바이트)·파일명 정리를 담당한다(외부 스토리지 I/O 전담).
 * S3 삭제는 트랜잭션 커밋 이후로 미루고, 업로드 실패/커밋 실패 시 보상 삭제를 제공한다.
 */
@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final S3Uploader s3Uploader;
    private final S3Remover s3Remover;

    /** 매직 바이트 검사용으로 읽을 앞부분 크기(WebP 시그니처가 12바이트라 12로 잡음). */
    private static final int HEADER_SIZE = 12;

    /**
     * 파일을 검증(매직 바이트)하고 S3에 업로드해 URL 목록을 반환한다. <b>트랜잭션 밖에서 호출한다.</b>
     * 반환 리스트의 index가 곧 [[img:new:K]]의 K다. 파일이 없으면 빈 리스트.
     * 업로드 도중 일부만 올라가고 실패하면, 이미 올린 것을 정리한 뒤 예외를 던진다(고아 객체 방지).
     */
    public List<String> upload(String subDirectory, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        validateImageFiles(images);

        List<String> uploaded = new ArrayList<>();
        try {
            for (MultipartFile file : images) {
                uploaded.add(uploadOne(file, subDirectory));
            }
            return uploaded;
        } catch (RuntimeException e) {
            removeNow(uploaded);   // 부분 업로드분 정리
            throw e;
        }
    }

    /**
     * 업로드 이후 DB 저장이 실패했을 때 이미 올라간 S3 객체를 즉시 제거하는 보상 삭제.
     * 트랜잭션은 이미 롤백된 상태이므로 바로 지운다. 삭제 중 실패는 원래 예외를 가리지 않도록 무시한다.
     */
    public void removeNow(List<String> urls) {
        if (urls == null) {
            return;
        }
        for (String url : urls) {
            try {
                s3Remover.remove(url);
            } catch (RuntimeException ignore) {
                // best-effort 정리
            }
        }
    }

    /**
     * S3 객체 제거를 트랜잭션 커밋 이후로 예약한다(트랜잭션이 없으면 즉시).
     * 느린 I/O를 트랜잭션 밖으로 빼고, 커밋에 성공한 경우에만 지워 "롤백됐는데 S3만 삭제됨"을 막는다.
     */
    public void removeAfterCommit(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    urls.forEach(s3Remover::remove);
                }
            });
        } else {
            urls.forEach(s3Remover::remove);
        }
    }

    /* ===================== S3/파일 ===================== */

    /**
     * 공용 S3Uploader로 한 장 업로드하고 공개 객체 URL을 반환한다.
     * 원본 파일명을 안전한 문자로 미리 정리해서 넘긴다 — S3Uploader가 파일명을 URLEncoder로 인코딩한 뒤
     * URL 생성 시 다시 인코딩해 "1 (8).jpg → 1%2520%25288%2529.jpg" 처럼 이중 인코딩되는 문제를 막기 위함.
     */
    private String uploadOne(MultipartFile file, String subDirectory) {
        MultipartFile safeNamed = new SanitizedNameFile(file, sanitizeFileName(file.getOriginalFilename()));
        return s3Uploader.uploadByFile(safeNamed, subDirectory).url();
    }

    /** 파일명에서 경로를 떼고 안전 문자([A-Za-z0-9._-])만 남긴다. 확장자(.)는 보존된다. */
    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "image";
        }
        String name = originalFileName.replace("\\", "/");
        name = name.substring(name.lastIndexOf('/') + 1);
        String cleaned = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.isBlank() ? "image" : cleaned;
    }

    private void validateImageFiles(List<MultipartFile> images) {
        for (MultipartFile file : images) {
            boolean declaredImage = file != null && !file.isEmpty()
                    && file.getContentType() != null && file.getContentType().startsWith("image/");
            // 선언된 Content-Type은 위조 가능하므로, 실제 파일 앞부분(매직 바이트)까지 이미지인지 확인한다.
            if (!declaredImage || !hasImageSignature(file)) {
                throw new InquiryException(InquiryErrorCode.IMAGE_FILE_INVALID);
            }
        }
    }

    /** 파일 앞부분을 읽어 실제 이미지 포맷(JPEG/PNG/GIF/WebP/BMP) 시그니처인지 판별한다. */
    private boolean hasImageSignature(MultipartFile file) {
        byte[] head = new byte[HEADER_SIZE];
        int read;
        try (InputStream is = file.getInputStream()) {
            read = is.readNBytes(head, 0, HEADER_SIZE);
        } catch (IOException e) {
            return false;
        }
        return isJpeg(head, read) || isPng(head, read) || isGif(head, read) || isWebp(head, read) || isBmp(head, read);
    }

    private boolean isJpeg(byte[] b, int n) {
        return n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] b, int n) {
        return n >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A;
    }

    private boolean isGif(byte[] b, int n) {
        return n >= 4 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8';
    }

    private boolean isWebp(byte[] b, int n) {
        return n >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }

    private boolean isBmp(byte[] b, int n) {
        return n >= 2 && b[0] == 'B' && b[1] == 'M';
    }

    /** 원본 MultipartFile을 그대로 위임하되 파일명만 안전하게 교체하는 래퍼. */
    private record SanitizedNameFile(MultipartFile delegate, String filename) implements MultipartFile {
        @Override public String getName() { return delegate.getName(); }
        @Override public String getOriginalFilename() { return filename; }
        @Override public String getContentType() { return delegate.getContentType(); }
        @Override public boolean isEmpty() { return delegate.isEmpty(); }
        @Override public long getSize() { return delegate.getSize(); }
        @Override public byte[] getBytes() throws IOException { return delegate.getBytes(); }
        @Override public InputStream getInputStream() throws IOException { return delegate.getInputStream(); }
        @Override public void transferTo(File dest) throws IOException, IllegalStateException { delegate.transferTo(dest); }
    }
}
