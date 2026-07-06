package com.stology.be.domain.study.entity;

import com.stology.be.domain.node.entity.Template;
import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Study extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;
    
    private String name;
    
    private String description;
    
    private String invitationLink;
    
    private Integer reviewerCount;
    
    @Builder.Default
    private Boolean isActive = true;
    
    private String studyLeader;
}
