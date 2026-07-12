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



    @Column(name = "week")
    private int week;

    @OneToMany(
            mappedBy = "nodeCandidate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<NodeCandidate2StudyMaterial> nodeCandidate2StudyMaterials
            = new ArrayList<>();


    public void increaseAcceptCount() {
        this.acceptCount++;
    }

    public void changeState(CandidateState state) {
        this.state = state;
    }


    public void addStudyMaterial(StudyMaterial studyMaterial) {
        NodeCandidate2StudyMaterial relation =
                NodeCandidate2StudyMaterial.create(this, studyMaterial);

        this.nodeCandidate2StudyMaterials.add(relation);
        studyMaterial.getNodeCandidate2StudyMaterials().add(relation);
    }

}
