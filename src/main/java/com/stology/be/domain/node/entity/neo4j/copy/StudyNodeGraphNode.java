package com.stology.be.domain.node.entity.neo4j.copy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Node("StudyNode")
public class StudyNodeGraphNode {

    @Id
    @Property("studyNodeId")
    private Long studyNodeId;

    @Property("originTemplateNodeId")
    private Long originTemplateNodeId;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    @Relationship(type = "SIMILAR_TO")
    private Set<StudyNodeRelation> relatedNodes = new HashSet<>();

    public StudyNode(
            Long studyNodeId,
            Long originTemplateNodeId,
            String title,
            String description
    ) {
        this.studyNodeId = studyNodeId;
        this.originTemplateNodeId = originTemplateNodeId;
        this.title = title;
        this.description = description;
    }
}