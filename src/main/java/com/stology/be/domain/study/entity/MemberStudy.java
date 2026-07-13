package com.stology.be.domain.study.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.entity.StudyMaterial;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberStudy extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToMany(
            mappedBy = "memberStudy",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    @Builder.Default
    private List<StudyMaterial> studyMaterials = new ArrayList<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;
}
