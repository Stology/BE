package com.stology.be.domain.upload.entity;

import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.upload.enums.FileType;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personal_study_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PersonalStudyData extends BaseEntity {

    @Id
    @Column(name = "member_study_id")
    private Long id;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_study_id", nullable = false)
    private MemberStudy memberStudy;

    @Column(nullable = false)
    @Builder.Default
    private Long week = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;

}