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
@Node("TemplateStudy")
public class TemplateStudyGraphNode {

    @Id
    @Property("studyId")
    private Long studyId;

    @Property("templateId")
    private Long templateId;

    @Relationship(type = "HAS_NODE")
    private Set<StudyNodeGraphNode> studyNodes = new HashSet<>();

    public TemplateStudyGraphNode(
            Long studyId,
            Long templateId
    ) {
        this.studyId = studyId;
        this.templateId = templateId;
    }
}