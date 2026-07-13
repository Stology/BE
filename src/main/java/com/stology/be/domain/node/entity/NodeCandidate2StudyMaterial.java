package com.stology.be.domain.node.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class NodeCandidate2StudyMaterial {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "node_candidate_study_material_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_candidate_id", nullable = false)
    private NodeCandidate nodeCandidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_material_id", nullable = false)
    private StudyMaterial studyMaterial;

    public static NodeCandidate2StudyMaterial create(
            NodeCandidate nodeCandidate,
            StudyMaterial studyMaterial
    ) {
        return NodeCandidate2StudyMaterial.builder()
                .nodeCandidate(nodeCandidate)
                .studyMaterial(studyMaterial)
                .build();
    }
}
