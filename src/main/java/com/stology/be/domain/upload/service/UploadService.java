package com.stology.be.domain.upload.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.exception.MemberException;
import com.stology.be.domain.member.exception.code.MemberErrorCode;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.upload.dto.req.UploadReq;
import com.stology.be.domain.upload.dto.res.GetSummaryRes;
import com.stology.be.domain.upload.dto.res.RecentFileRes;
import com.stology.be.domain.upload.dto.res.RecentFilesRes;
import com.stology.be.domain.upload.event.ReTaskEvent;
import com.stology.be.domain.upload.event.UploadedEvent;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.domain.upload.exception.UploadException;
import com.stology.be.domain.upload.exception.code.UploadErrorCode;
import com.stology.be.global.external.s3.S3Uploader;
import com.stology.be.global.external.s3.dto.S3InfoDto;
import com.stology.be.global.security.entity.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class UploadService {

    private final MemberRepository memberRepository;
    private final StudyMaterialRepository studyMaterialRepository;
    private final MemberStudyRepository memberStudyRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final S3Uploader s3Uploader;


    private static final int RECENT_FILE_COUNT = 4;


    @Transactional
    public void upload(
            Long studyId,
            Long memberId,
            UploadReq request
    ) {
        //1. 검증 업로더가 스터디 안에 맴버인지 검증  2. 파일이(비었는지, md인지)
        MemberStudy memberStudy = getMemberStudy(studyId, memberId);

        MultipartFile file = request.getFile();
        String content = request.getDescription();

        boolean hasFile = file != null && !file.isEmpty();
        boolean hasContent = content != null && !content.isBlank();

        //파일과 컨텐츠가 아무것도 없을떄.
        if (!hasFile && !hasContent) {
            throw new UploadException(
                    UploadErrorCode.UPLOAD_CONTENT_EMPTY
            );
        }
        S3InfoDto s3Info = null;
        //파일이 있을때 저장
        if (hasFile) {
            validateMarkdownExtension(file);
            validateUtf8Encoding(file);

            s3Info = uploadToS3(file,studyId);
        }

        //설명이 없을 떄
        if (!hasContent) {
            content = "";
        }

        //2. DB에 개인자료 저장 N저장
        Member member = getMember(memberId);

        StudyMaterial studyMaterial = StudyMaterial.builder()
                .dataState(DataState.READY)
                .memberStudy(memberStudy)
                .dataTitle(request.getTitle())
                .content(content)
                .fileUrl(s3Info != null ? s3Info.url() : null)
                .objectKey(s3Info != null ? s3Info.objectKey() : null)
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
                        .createdAt(studyMaterial.getCreatedAt())
                        .build()
        );




    }
    public RecentFilesRes getStudyUploadFiles(
            Long studyId,
            Long memberId
    ) {

        //검증
        getMemberStudy(studyId, memberId);


        List<RecentFileRes> files =
                studyMaterialRepository.findRecentFilesByStudyId(
                        studyId,
                        PageRequest.of(0, RECENT_FILE_COUNT)
                );

        return new RecentFilesRes(files);

    }
    public GetSummaryRes getMaterialSummary(
            Long studyId,
            Long studyMaterialId,
            AuthMember authMember
    ){
        // 1. 검증
        getMemberStudy(studyId, authMember.getMemberId());

        // 2 스터디 메터리얼 찾기 + 검증
        StudyMaterial studyMaterial = getStudyMaterial(studyMaterialId);
        //
        if (studyMaterial.getSummary() == null || studyMaterial.getSummary().isEmpty()) {
            throw new UploadException(UploadErrorCode.AI_SUMMARY_NOT_COMPLETE);
        }
        return new GetSummaryRes(studyMaterial.getSummary());
    }

    @Transactional
    public void reAnalyzeMaterial(
            Long studyId,
            Long studyMaterialId,
            AuthMember authMember
    ){
        // 1. 검증
        MemberStudy memberStudy = getMemberStudy(studyId, authMember.getMemberId());

        // 2. 스터디 메터리얼 찾기 + 검증
        StudyMaterial studyMaterial = getStudyMaterial(studyMaterialId);



        // 3. 본인의 자료가 맞는지.
        if (!memberStudy.getId().equals(
                studyMaterial.getMemberStudy().getId()
        )) {
            throw new UploadException(
                    UploadErrorCode.NO_GRANDTED_FOR_STUDY_MATERIAL
            );
        }

        // 4. 추출 중이면 이중 추출 방지를 위함 코드 + 실패한 경우에만 재추출 허락하는 것.
        if (studyMaterial.getDataState() != DataState.EXTRACTIONFAILED) {
            throw new UploadException(
                    UploadErrorCode.AI_SUMMARY_NOT_COMPLETE
            );
        }
        studyMaterial.changeDataState(DataState.EXTRACTING);


        eventPublisher.publishEvent(
                ReTaskEvent.builder()
                        .studyId(studyId)
                        .studyMaterialId(studyMaterialId)
                        .uploaderMemberId(authMember.getMemberId())
                        .build()
        );
    }





    /*
    내부 함수
     */

    private void validateMarkdownFile(
            MultipartFile file
    ) {
        validateFileExists(file);
        validateMarkdownExtension(file);
        validateUtf8Encoding(file);
    }

    private void validateFileExists(
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new UploadException(
                    UploadErrorCode.UPLOAD_FILE_EMPTY
            );
        }
    }

    private void validateMarkdownExtension(
            MultipartFile file
    ) {
        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null ||
                !originalFilename
                        .toLowerCase(Locale.ROOT)
                        .endsWith(".md")) {

            throw new UploadException(
                    UploadErrorCode
                            .UPLOAD_FILE_EXTENSION_INVALID
            );
        }
    }

    private void validateUtf8Encoding(
            MultipartFile file
    ) {
        try {
            StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(
                            CodingErrorAction.REPORT
                    )
                    .onUnmappableCharacter(
                            CodingErrorAction.REPORT
                    )
                    .decode(
                            ByteBuffer.wrap(
                                    file.getBytes()
                            )
                    );

        } catch (CharacterCodingException e) {
            throw new UploadException(
                    UploadErrorCode
                            .UPLOAD_FILE_ENCODING_INVALID
            );

        } catch (IOException e) {
            throw new UploadException(
                    UploadErrorCode
                            .UPLOAD_FILE_READ_FAILED
            );
        }
    }

    private String readMarkdown(
            MultipartFile file
    ) {
        try {
            return new String(
                    file.getBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new UploadException(
                    UploadErrorCode.UPLOAD_FILE_READ_FAILED
            );
        }
    }

    private S3InfoDto uploadToS3(
            MultipartFile file,
            Long studyId
    ) {
        try {
            return s3Uploader.uploadByFile(
                    file,
                    "study-material/" + studyId
            );

        } catch (Exception e) {
            throw new UploadException(
                    UploadErrorCode.UPLOAD_S3_FAILED
            );
        }
    }

    private Member getMember(
            Long memberId
    ) {
        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberStudy getMemberStudy(
            Long studyId,
            Long memberId
    ) {
        return memberStudyRepository
                .findByStudyIdAndMemberId(
                        studyId,
                        memberId
                )
                .orElseThrow(
                        () -> new UploadException(
                                UploadErrorCode
                                        .UPLOAD_MEMBER_NOT_IN_STUDY
                        )
                );
    }
    private StudyMaterial getStudyMaterial(Long studyMaterialId) {

        return studyMaterialRepository
                .findById(studyMaterialId).orElseThrow(
                        () -> new UploadException(
                                UploadErrorCode
                                        .STUDY_MATERIAL_NOT_FOUND));
    }
}




