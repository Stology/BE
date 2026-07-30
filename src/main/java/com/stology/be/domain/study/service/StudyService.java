package com.stology.be.domain.study.service;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.member.repository.MemberRepository;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.node.repository.StudyMaterialRepository;
import com.stology.be.domain.node.repository.StudyNodeRepository;
import com.stology.be.domain.report.repository.QuestionRepository;
import com.stology.be.domain.study.converter.StudyConverter;
import com.stology.be.domain.study.dto.StudyReqDTO;
import com.stology.be.domain.study.dto.StudyResDTO;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.domain.study.event.StudyCreatedEvent;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.domain.study.repository.MemberStudyRepository;
import com.stology.be.domain.study.repository.StudyRepository;
import com.stology.be.domain.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final TemplateRepository templateRepository;
    private final MemberStudyRepository memberStudyRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher publisher;
    private final StudyMaterialRepository studyMaterialRepository;
    private final QuestionRepository questionRepository;
    private final StudyNodeRepository studyNodeRepository;

    private static final String STOLOGY_URL = "https://stology.vercel.app";

    // 스터디 방 생성
    public Long createStudy(StudyReqDTO.CreateStudy dto, Member member) {
        // 템플릿 조회
        Template template = templateRepository.findById(dto.templateId())
                .orElseThrow(() -> new StudyException(StudyErrorCode.TEMPLATE_NOT_FOUND));
        // 스터디 이름 중복 확인
        if(studyRepository.existsByName(dto.name())) {
            throw new StudyException(StudyErrorCode.STUDY_NAME_DUPLICATE);
        }
        // 00시 00분으로 시간 설정
        LocalDateTime startDateTime = dto.startDate().atStartOfDay();
        // 스터디 방 생성
        Study study = StudyConverter.toCreateStudy(dto, template, member, startDateTime);
        studyRepository.save(study);

        // MemberStudy 유저 생성
        MemberStudy memberStudy = StudyConverter.toCreateMemberStudy(study, member);
        memberStudyRepository.save(memberStudy);

        // 스터디 템플릿 복제 작업(트랜잭션 이벤트 처리)
        publisher.publishEvent(new StudyCreatedEvent(study.getId(),template.getId()));

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
        // 날짜만 수정
        LocalDateTime updatedStartDate = dto.startDate().atTime(study.getStartDate().toLocalTime());
        // 스터디방 정보 수정
        if(dto.name()!=null&&!study.getName().equals(dto.name())){
            if(studyRepository.existsByName(dto.name())) {
                throw new StudyException(StudyErrorCode.STUDY_NAME_DUPLICATE);
            }
        }
        study.update(dto, updatedStartDate);
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
    public StudyResDTO.CloseStudy closeStudy(Long studyId, Member member) {
        // 스터디 조회
        Study study = findStudy(studyId);
        // 스터디장 권한
        study.validateLeader(member);
        // 스터디 유효성 검사
        validateStudy(study);
        // 스터디 종료
        study.close();
        // 총 활성 노드의 수
        Integer activeNodeCount = studyNodeRepository.countByStudy_IdAndActiveLevelGreaterThan(studyId, 0);
        // 업로드된 자료의 수
        Integer uploadedMaterialCount = studyMaterialRepository.countReadyByStudyId(studyId);
        // 작성된 질문의 수
        Integer questionCount = questionRepository.countByStudyId(studyId);
        return StudyConverter.toCloseStudy(activeNodeCount, uploadedMaterialCount, questionCount);
    }

    // 참여한 스터디 방 목록 조회
    public StudyResDTO.GetStudy getStudy(String status, Member member) {
        // 유저 스터디 조회
        List<Study> studies = memberStudyRepository.findByMember(member).stream()
                .map(MemberStudy::getStudy)
                .filter(study -> study.getDeletedAt() == null)
                .toList();
        // 상태 필터링
        if ("active".equals(status)) {
            studies = studies.stream()
                    .filter(Study::getIsActive)
                    .toList();
        } else if ("closed".equals(status)) {
            studies = studies.stream()
                    .filter(study -> !study.getIsActive())
                    .toList();
        }
        // 최근 1일 기준
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        // 최근 자료가 있는 studyId 조회
        List<Long> studyIds = studies.stream()
                .map(Study::getId)
                .toList();
        List<Long> newStudyIds = studyIds.isEmpty()
                ? List.of()
                : studyMaterialRepository.findNewStudyIds(studyIds, oneDayAgo);
        Set<Long> newStudyIdSet = new HashSet<>(newStudyIds);
        List<StudyResDTO.Study> studyList = studies.stream()
                .sorted(Comparator.comparingLong(Study::getId))
                .map(study -> new StudyResDTO.Study(
                        study.getId(),
                        study.getName(),
                        study.getStartDate(),
                        newStudyIdSet.contains(study.getId())
                ))
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
        // 이미 초대 토큰이 존재하는 경우 그 초대 토큰 반환
        if(study.getInvitationToken()!=null){
            return STOLOGY_URL + "/invite/" + study.getInvitationToken();
        }
        // 초대 토큰 생성
        String token = UUID.randomUUID().toString();
        // 토큰 저장
        study.createToken(token);
        return STOLOGY_URL + "/invite/" + token;
    }

    // 초대 토큰 조회
    public StudyResDTO.GetInvitationToken getInvitationToken(String token) {
        // 스터디 토큰 유효성 검사
        Study study = studyRepository.findByInvitationToken(token)
                .orElseThrow(() -> new StudyException(StudyErrorCode.INVITATION_TOKEN_NOT_FOUND));
        // 스터디 토큰 유효성 검사
        validateStudy(study);
        if(study.getInvitationToken().isEmpty()){
            throw new StudyException(StudyErrorCode.INVITATION_TOKEN_NOT_FOUND);
        }
        // 스터디 인원
        Integer memberCount = memberStudyRepository.countByStudyId(study.getId());
        // 스터디장 조회
        Member leader = memberRepository.findById(study.getLeaderMemberId())
                .orElseThrow(()-> new StudyException(StudyErrorCode.LEADER_NOT_FOUND));

        return StudyConverter.toGetInvitationToken(memberCount, study, leader);
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
        if(!study.getIsActive()){
            throw new StudyException(StudyErrorCode.STUDY_ALREADY_CLOSED);
        } else if(study.getDeletedAt()!=null){
            throw new StudyException(StudyErrorCode.STUDY_ALREADY_DELETED);
        }
    }
}
