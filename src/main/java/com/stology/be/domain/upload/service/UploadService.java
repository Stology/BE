package com.stology.be.domain.upload.service;

import com.stology.be.domain.upload.dto.req.UploadReq;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

public class UploadService {


    @Transactional
    public void upload(
            Long studyId,
            Long studyNodeId,
            UploadReq request
    ) {


        validateMarkdownFile(request.getFile());

        String content = readMarkdown(request.getFile());


        String fileUrl = fileStorageService.upload(request.getFile());

        // StudyMaterial, NodeCandidate 저장


    }



    private void validateMarkdownFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "업로드할 파일이 존재하지 않습니다."
            );
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null ||
                !originalFilename.toLowerCase(Locale.ROOT).endsWith(".md")) {
            throw new IllegalArgumentException(
                    "Markdown(.md) 파일만 업로드할 수 있습니다."
            );
        }

        try {
            new String(
                    file.getBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "올바른 Markdown 텍스트 파일이 아닙니다."
            );
        }
    }
}