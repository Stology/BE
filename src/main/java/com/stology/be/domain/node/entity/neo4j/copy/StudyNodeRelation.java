package com.stology.be.domain.node.entity.neo4j.copy;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RelationshipProperties
public class StudyNodeRelation {

    @Id
    @GeneratedValue
    private Long id;

    private String relation;

    @TargetNode
    private StudyNodeGraphNode targetNode;

    public StudyNodeRelation(
            String relation,
            StudyNodeGraphNode targetNode
    ) {
        this.relation = relation;
        this.targetNode = targetNode;
    }
}