package com.stology.be.domain.study.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.study.converter.StudyConverter;
import com.stology.be.domain.study.dto.StudyReqDTO;
import com.stology.be.domain.study.dto.StudyResDTO;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.study.repository.StudyRepository;
import com.stology.be.domain.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final MemberRepository memberRepository;
    private final TemplateRepository templateRepository;
    private final MemberStudyRepository memberStudyRepository;

    private static final String STOLOGY_URL = "https://stology.com";

    // 스터디 방 생성
    public Long createStudy(StudyReqDTO.CreateStudy dto, Member member) {
        // 템플릿 조회
        Template template = templateRepository.findById(dto.templateId())
                .orElseThrow(() -> new StudyException(StudyErrorCode.TEMPLATE_NOT_FOUND));
        // 스터디 이름 중복 확인
        if(studyRepository.existsByName(dto.name())) {
            throw new StudyException(StudyErrorCode.STUDY_NAME_DUPLICATE);
        }
        // 스터디 방 생성
        Study study = StudyConverter.toCreateStudy(dto, template, member);
        studyRepository.save(study);
        // MemberStudy 유저 생성
        MemberStudy memberStudy = StudyConverter.toCreateMemberStudy(study, member);
        memberStudyRepository.save(memberStudy);
        return study.getId();
    }

    // 스터디 방 정보 수정
    public Void updateStudy(StudyReqDTO.UpdateStudy dto, Long studyId, Member member) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // 스터디 유효성 확인
        validateStudy(study);
        // 스터디장 확인
        study.validateLeader(member);
        // 스터디방 정보 수정
        if(dto.name()!=null&&!study.getName().equals(dto.name())){
            if(studyRepository.existsByName(dto.name())) {
                throw new StudyException(StudyErrorCode.STUDY_NAME_DUPLICATE);
            }
        }
        study.update(dto);
        return null;
    }

    // 스터디 방 삭제
    public Void deleteStudy(Long studyId, Member member) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // 스터디장 권한
        study.validateLeader(member);
        // 스터디 유효성 검사
        validateStudy(study);
        // deletedAt 수정
        study.delete();
        return null;
    }

    // 스터디 종료
    public Void closeStudy(Long studyId, Member member) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // 스터디장 권한
        study.validateLeader(member);
        // 스터디 유효성 검사
        validateStudy(study);
        // 스터디 종료
        study.close();
        return null;
    }

    // 참여한 스터디 방 목록 조회
    public StudyResDTO.GetStudy getStudy(String status, Member member) {
        // 유저가 참여한 스터디
        List<Study> studies = memberStudyRepository.findByMember(member).stream()
                .map(MemberStudy::getStudy)
                .toList();
        Stream<Study> stream = studies.stream()
                .filter(study -> study.getDeletedAt() == null);
        // status 필터링
        if ("active".equals(status)) {
            stream = stream.filter(Study::getIsActive);
        } else if ("closed".equals(status)) {
            stream = stream.filter(study -> !study.getIsActive());
        }
        List<StudyResDTO.Study> studyList = stream
                .sorted(Comparator.comparingLong(Study::getId))
                .map(study -> new StudyResDTO.Study(
                        study.getId(),
                        study.getName(),
                        study.getStartDate(),
                        study.getDescription(),
                        study.getIsActive()))
                .toList();
        return new StudyResDTO.GetStudy(studyList);
    }

    // 온톨로지 템플릿 검색
    public StudyResDTO.GetTemplate getTemplate(String search) {
        List<Template> templates;
        Stream<Template> stream = templateRepository.findAll().stream();
        if(search == null || search.isEmpty()){
            templates = templateRepository.findAll();
        } else {
            templates = templateRepository.findByNameContainingIgnoreCase(search.trim());
        }
        List<StudyResDTO.Template> templateList = templates.stream()
                .map(template -> new StudyResDTO.Template(
                        template.getId(),
                        template.getName(),
                        template.getUploader().getName(),
                        template.getDescription()))
                .toList();
         return new StudyResDTO.GetTemplate(templateList);
    }

    // 검토 인원수 조회
    public StudyResDTO.GetReviewerCount getReviewerCount(Long studyId) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // reviewerCount
        Integer count = study.getReviewerCount();
        // maxReviewerCount
        Integer maxCount = memberStudyRepository.countByStudyId(study.getId());
        return StudyConverter.toGetReviewerCount(count, maxCount);
    }

    // 검토 인원수 조정
    public Void updateReviewerCount(Long studyId, StudyReqDTO.UpdateReviewerCount dto, Member member) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // 스터디장 권한
        study.validateLeader(member);
        // maxReviewerCount
        Integer maxCount = memberStudyRepository.countByStudyId(study.getId());
        if(dto.reviewerCount()>maxCount){
            throw new StudyException(StudyErrorCode.REVIEWER_COUNT_EXCEEDED);
        }
        study.updateReviewer(dto.reviewerCount());
        return null;
    }

    // 초대 토큰 생성
    public String createInvitationToken(Long studyId, Member member) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // 스터디장 권한
        study.validateLeader(member);
        // 스터디 유효성 검사
        validateStudy(study);
        // 이미 초대 토큰이 존재할 경우 예외 처리
        if(study.getInvitationToken()!=null){
            throw new StudyException(StudyErrorCode.INVITATION_TOKEN_ALREADY_EXISTS);
        }
        // 초대 토큰 생성
        String token = UUID.randomUUID().toString();
        // 토큰 저장
        study.createToken(token);
        return STOLOGY_URL + "/invite/" + token;
    }

    // 초대 토큰 조회
    public String getInvitationToken(Long studyId, Member member) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // 스터디장 권한
        study.validateLeader(member);
        // 스터디 토큰 유효성 검사
        validateStudy(study);
        if(study.getInvitationToken().isEmpty()){
            throw new StudyException(StudyErrorCode.INVITATION_TOKEN_NOT_FOUND);
        }
        return STOLOGY_URL + "/invite/" + study.getInvitationToken();
    }

    // 초대 토큰 수락
    public Void acceptInvitationToken(String token, Member member) {
        Study study = studyRepository.findByInvitationToken(token)
                .orElseThrow(() -> new StudyException(StudyErrorCode.INVITATION_TOKEN_NOT_FOUND));
        // 스터디 토큰 유효성 검사
        validateStudy(study);
        if(memberStudyRepository.existsByMemberAndStudy(member, study)) {
            throw new StudyException(StudyErrorCode.INVITATION_ALREADY_JOINED);
        }
        // MemerStudy 유저 생성
        MemberStudy memberStudy = StudyConverter.toCreateMemberStudy(study,member);
        memberStudyRepository.save(memberStudy);
        return null;
    }

    // 스터디 조회
    private Study findStudy(Long studyId){
        return studyRepository.findById(studyId).orElseThrow(() -> new StudyException(StudyErrorCode.STUDY_NOT_FOUND));
    }

    // 스터디 유효성 검사
    private void validateStudy(Study study){
        if(study.getDeletedAt()!=null){
            throw new StudyException(StudyErrorCode.STUDY_ALREADY_DELETED);
        } else if(!study.getIsActive()){
            throw new StudyException(StudyErrorCode.STUDY_ALREADY_CLOSED);
        }
    }
}
