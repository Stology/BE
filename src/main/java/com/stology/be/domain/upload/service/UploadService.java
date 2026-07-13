package com.stology.be.domain.upload.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.repository.NodeCandidateRepository;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.repository.MemberStudyRepository;
import com.stology.be.domain.upload.dto.req.UploadReq;
import com.stology.be.domain.upload.dto.res.RecentFileRes;
import com.stology.be.domain.upload.dto.res.RecentFilesRes;
import com.stology.be.domain.upload.event.UploadedEvent;
import com.stology.be.domain.upload.enums.DataState;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class UploadService {

    private final MemberRepository memberRepository;
    private final StudyNodeRepository studyNodeRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final NodeCandidateRepository nodeCandidateRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final int RECENT_FILE_COUNT = 4;

    @Transactional
    public void upload(
            Long studyId,
            Long memberId,
            UploadReq request
    ) {
        //검증
        MemberStudy memberStudy = getMemberStudy(studyId, memberId);
        validateMarkdownFile(request.getFile());

        //변환
        String content = readMarkdown(request.getFile());

        //1. S3에 파일 저장
        String fileUrl = null;
        /* 나중에 구현 일단 null
        String fileUrl = fileStorageService.upload(request.getFile());
         */
        //2. DB에 개인자료 저장 N저장
        Member member = memberRepository.findById(memberId).orElse(null);

        StudyMaterial studyMaterial = StudyMaterial.builder()
                .dataState(DataState.READY)
                .memberStudy(memberStudy)
                .dataTitle(request.getTitle())
                .content(content)
                .fileUrl(fileUrl)
                .build();

        studyMaterialRepository.save(studyMaterial);
        //3. SSE로 모든 후보에 자료 업로드 되었다고 말하기
        //4. AI 요청 이벤트 하기
        //이벤트 리스너 등록
        eventPublisher.publishEvent(
                UploadedEvent.builder()
                        .studyId(studyId)
                        .studyMaterialId(studyMaterial.getId())
                        .uploaderMemberId(memberId)
                        .uploaderName(member.getName())
                        .dataTitle(request.getTitle())
                        .week(request.getWeek())
                        .createdAt(studyMaterial.getCreatedAt())
                        .build()
        );




    }
    public RecentFilesRes getStudyUploadFiles(
            Long studyId,
            Long memberId
    ) {
        MemberStudy memberStudy = getMemberStudy(studyId, memberId);


        List<RecentFileRes> files =
                studyMaterialRepository.findRecentFilesByStudyId(
                        studyId,
                        PageRequest.of(0, RECENT_FILE_COUNT)
                );

        return new RecentFilesRes(files);

    }




    /*
    내부 함수
     */

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

    private String readMarkdown(MultipartFile file) {
        try {
            return new String(
                    file.getBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Markdown 파일을 읽을 수 없습니다.",
                    e
            );
        }
    }
    private MemberStudy getMemberStudy(Long studyId, Long memberId) {
        MemberStudy memberStudy = memberStudyRepository
                .findByStudyIdAndMemberId(studyId, memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 스터디에 참여한 회원이 아닙니다."
                        )
                );
        return memberStudy;
    }




}