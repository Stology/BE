package com.stology.be.domain.node.entity.neo4j;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

@Node("TemplateNode")
@Getter
@NoArgsConstructor
public class TemplateNodeGraphNode {

    @Id
    @GeneratedValue
    private Long templateNodeId;

    private String title;

    private int week;

    private String description;

    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<TemplateNodeRelation> relatedNodes = new HashSet<>();

    public TemplateNodeGraphNode(String name,int week,String description) {
        this.title = name;
        this.week = week;
        this.description = description;
    }


    public void addRelation(
            String relation,
            TemplateNodeGraphNode targetNode
    ) {
        TemplateNodeRelation nodeRelation =
                new TemplateNodeRelation(
                        relation,
                        targetNode
                );

        this.relatedNodes.add(nodeRelation);
    }
}