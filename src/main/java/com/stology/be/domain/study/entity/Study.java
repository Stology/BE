package com.stology.be.domain.study.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.study.dto.StudyReqDTO;
import com.stology.be.domain.study.exception.StudyException;
import com.stology.be.domain.study.exception.code.StudyErrorCode;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Study extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;

    private String name;

    private String description;

    private String invitationToken;

    @Builder.Default
    private Integer reviewerCount=0;

    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "leader_member_id")
    private Long leaderMemberId;

    private LocalDate startDate;

    // 스터디장 확인
    public void validateLeader(Member member){
        if(!leaderMemberId.equals(member.getId())){
            throw new StudyException(StudyErrorCode.STUDY_LEADER_ACCESS_DENIED);
        }
    }
    // 스터디 수정
    public void update(StudyReqDTO.UpdateStudy dto) {
        this.name = dto.name();
        this.description = dto.description();
        this.startDate = dto.startDate();
    }
    // 스터디 종료
    public void close(){
        this.isActive = false;
    }
    // 검토 인원수 조정
    public void updateReviewer(Integer count){
        this.reviewerCount = count;
    }
    // 토큰 저장
    public void createToken(String token){
        this.invitationToken =  token;
    }
}

