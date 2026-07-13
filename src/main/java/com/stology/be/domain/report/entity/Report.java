package com.stology.be.domain.report.entity;

import com.stology.be.domain.report.converter.MemberActivityStatisticsListConverter;
import com.stology.be.domain.report.converter.RecommendedNodeListConverter;
import com.stology.be.domain.report.converter.WeeklyCoreNodeListConverter;
import com.stology.be.domain.report.dto.MemberActivityStatisticsDto;
import com.stology.be.domain.report.dto.RecommendedNodeDto;
import com.stology.be.domain.report.dto.WeeklyCoreNodeDto;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

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
    @Convert(converter = WeeklyCoreNodeListConverter.class)
    private List<WeeklyCoreNodeDto> weeklyCoreNodeList;
    
    @Column(columnDefinition = "text")
    private String aiReviewContent;
    
    @Column(columnDefinition = "json")
    @Convert(converter = RecommendedNodeListConverter.class)
    private List<RecommendedNodeDto> recommendedNodeList;
    
    @Column(columnDefinition = "text")
    private String followUpContent;
    
    @Column(columnDefinition = "json")
    @Convert(converter = MemberActivityStatisticsListConverter.class)
    private List<MemberActivityStatisticsDto> memberActivityStatisticsList;
}
