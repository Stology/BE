package com.stology.be.domain.node.entity;

import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NodeCandidate extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_material_id")
    private StudyMaterial studyMaterial;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_node_id")
    private StudyNode studyNode;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CandidateState state = CandidateState.PENDING;
    
    @Builder.Default
    private Integer acceptCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_study_id")
    private MemberStudy memberStudy;

    @Lob
    private String content;

    @Column(name = "file_url")
    private String fileUrl;

    public void increaseAcceptCount() {
        this.acceptCount++;
    }

    public void changeState(CandidateState state) {
        this.state = state;
    }

}
