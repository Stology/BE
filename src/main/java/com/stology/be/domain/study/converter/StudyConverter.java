package com.stology.be.domain.study.converter;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.study.dto.StudyReqDTO;
import com.stology.be.domain.study.dto.StudyResDTO;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Study;

import java.time.LocalDateTime;

public class StudyConverter {
    // 스터디 방 생성
    public static Study toCreateStudy(
            StudyReqDTO.CreateStudy dto,
            Template template,
            Member member,
            LocalDateTime startDateTime
    ){
        return Study.builder()
                .name(dto.name())
                .description(dto.description())
                .leaderMemberId(member.getId())
                .startDate(startDateTime)
                .template(template)
                .build();
    }

    // 검토 인원수 조회
    public static StudyResDTO.GetReviewerCount toGetReviewerCount(
            Integer count,
            Integer maxCount
    ){
        return StudyResDTO.GetReviewerCount.builder()
                .reviewerCount(count)
                .maxReviewerCount(maxCount)
                .build();
    }

    // MemberStudy 유저 생성
    public static MemberStudy toCreateMemberStudy(
            Study study,
            Member member
    ){
        return MemberStudy.builder()
                .member(member)
                .study(study)
                .build();
    }

    // 초대 토큰 조회
    public static StudyResDTO.GetInvitationToken toGetInvitationToken(Integer memberCount, Study study, Member leader) {
        return StudyResDTO.GetInvitationToken.builder()
                .studyId(study.getId())
                .name(study.getName())
                .leader(leader.getName())
                .memberCount(memberCount)
                .build();
    }

    public static StudyResDTO.CloseStudy toCloseStudy(Integer activeNodeCount, Integer uploadedMaterialCount, Integer questionCount) {
        return StudyResDTO.CloseStudy.builder()
                .activeNodeCount(activeNodeCount)
                .questionCount(questionCount)
                .uploadedMaterialCount(uploadedMaterialCount)
                .build();
    }
}
