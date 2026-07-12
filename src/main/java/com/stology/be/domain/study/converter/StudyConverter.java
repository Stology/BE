package com.stology.be.domain.study.converter;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.study.dto.StudyReqDTO;
import com.stology.be.domain.study.dto.StudyResDTO;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Study;

public class StudyConverter {
    // 스터디 방 생성
    public static Study toCreateStudy(
            StudyReqDTO.CreateStudy dto,
            Template template, Member member
    ){
        return Study.builder()
                .name(dto.name())
                .description(dto.description())
                .leader(member)
                .startDate(dto.startDate())
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
}
