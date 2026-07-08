package com.stology.be.domain.study.entity;

import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Report extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;
    
    @Builder.Default
    private Integer totalNodeCount = 0;
    
    @Builder.Default
    private Integer newActiveNodeCount = 0;
    
    @Builder.Default
    private Integer reinforcedNodeCount = 0;
    
    @Column(columnDefinition = "json")
    private String weeklyCoreNodeList;
    
    @Column(columnDefinition = "text")
    private String aiReviewContent;
    
    @Column(columnDefinition = "json")
    private String recommendedNodeList;
    
    @Column(columnDefinition = "text")
    private String followUpContent;
    
    @Column(columnDefinition = "json")
    private String memberActivityStatisticsList;
}
