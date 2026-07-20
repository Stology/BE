package com.stology.be.domain.node.entity.neo4j.copy;

mport lombok.AccessLevel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RelationshipProperties
public class StudyNodeRelation {

    @RelationshipId
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