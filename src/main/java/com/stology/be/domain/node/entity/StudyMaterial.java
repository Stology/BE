package com.stology.be.domain.node.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.upload.enums.DataState;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_material")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StudyMaterial extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_material_id")
    private Long id;

    @Column(name = "file_url")
    private String fileUrl;

    @Lob
    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_state", nullable = false)
    @Builder.Default
    private DataState dataState = DataState.READY;

    @Column(name = "data_title")
    private String dataTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;


    @OneToMany(
            mappedBy = "studyMaterial",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<NodeCandidate2StudyMaterial> nodeCandidate2StudyMaterials
            = new ArrayList<>();

    public void changeDataState(DataState dataState) {
        this.dataState = dataState;
    }

    public void addNodeCandidate(NodeCandidate nodeCandidate) {
        NodeCandidate2StudyMaterial relation =
                NodeCandidate2StudyMaterial.create(nodeCandidate, this);

        this.nodeCandidate2StudyMaterials.add(relation);
        nodeCandidate.getNodeCandidate2StudyMaterials().add(relation);
    }
}