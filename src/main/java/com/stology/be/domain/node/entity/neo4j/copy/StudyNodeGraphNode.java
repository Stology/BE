package com.stology.be.domain.node.entity.neo4j.copy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Node("StudyNode")
public class StudyNodeGraphNode {

    @Id
    @GeneratedValue
    private Long studyNodeId;


    @Property("title")
    private String title;


    @Relationship(type = "SIMILAR_TO")
    private Set<StudyNodeRelation> relatedNodes = new HashSet<>();

    public StudyNodeGraphNode(
            Long studyNodeId,
            String title
    ) {
        this.studyNodeId = studyNodeId;
        this.title = title;
    }

    public void addRelatedNode(
            String relation,
            StudyNodeGraphNode targetNode
    ) {
        this.relatedNodes.add(
                new StudyNodeRelation(
                        relation,
                        targetNode
                )
        );
    }
}