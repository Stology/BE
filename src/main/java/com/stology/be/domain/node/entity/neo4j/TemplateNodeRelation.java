package com.stology.be.domain.node.entity.neo4j;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Getter
@NoArgsConstructor
public class TemplateNodeRelation {
    /* DB 구조도.
    (:Template)
    -[:HAS_NODE]->
    (:TemplateNode)

    (:TemplateNode)
    -[:RELATED_TO {relation: "특정 개념"}]->
    (:TemplateNode)
     */

    @RelationshipId
    private Long id;

    private String relation;

    @TargetNode
    private TemplateNodeGraphNode targetNode;
}