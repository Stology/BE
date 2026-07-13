package com.stology.be.domain.node.entity;

import com.stology.be.domain.node.enums.CandidateState;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.upload.enums.DataState;
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
public class NodeCandidate extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CandidateState state = CandidateState.PENDING;

    
    @Builder.Default
    private Integer acceptCount = 0;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_node_id", nullable = false)
    private StudyNode studyNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_material_id", nullable = false)
    private StudyMaterial studyMaterial;


    public void increaseAcceptCount() {
        this.acceptCount++;
    }

    public void changeState(CandidateState state) {
        this.state = state;
    }




}
