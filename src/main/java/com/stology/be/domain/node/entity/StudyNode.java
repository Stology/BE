package com.stology.be.domain.node.entity;

import com.stology.be.domain.study.entity.Study;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StudyNode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;


    @Column(nullable = false)
    private String title;

    //설명
    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private int activationWeek = 0;

    @Builder.Default
    private Integer activeLevel = 0;

    @Column(name = "recommend_week")
    private Integer recommendWeek;

    public static StudyNode createFromTemplate(
            Study study,
            String title,
            int recommendWeek,
            String description
    ) {
        return StudyNode.builder()
                .study(study)
                .title(title)
                .recommendWeek(recommendWeek)
                .description(description)
                .build();

    }
    public void increaseActiveLevel() {
        this.activeLevel++;
    }
    public void decreaseActiveLevel() {
        this.activeLevel--;
    }
}

