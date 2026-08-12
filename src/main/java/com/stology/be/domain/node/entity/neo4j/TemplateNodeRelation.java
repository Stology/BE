package com.stology.be.domain.node.entity.neo4j;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

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

    @Id
    @GeneratedValue
    private Long id;

    @Property("relation")
    private String relation;

    @TargetNode
    private TemplateNodeGraphNode targetNode;

    public TemplateNodeRelation(
            String relation,
            TemplateNodeGraphNode targetNode
    ) {
        this.relation = relation;
        this.targetNode = targetNode;
    }
}