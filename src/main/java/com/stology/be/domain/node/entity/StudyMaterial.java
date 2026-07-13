package com.stology.be.domain.node.entity;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.study.entity.MemberStudy;
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

    @Column(name = "week")
    private int week;


    @ManyToOne(fetch = FetchType.LAZY)
    private MemberStudy memberStudy;


    @OneToMany(
            mappedBy = "studyMaterial",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<NodeCandidate> nodeCandidates = new ArrayList<>();

    public void changeDataState(DataState dataState) {
        this.dataState = dataState;
    }


}