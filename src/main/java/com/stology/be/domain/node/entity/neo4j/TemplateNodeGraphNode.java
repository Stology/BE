package com.stology.be.domain.node.entity.neo4j;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("TemplateNode")
@Getter
@NoArgsConstructor
public class TemplateNodeGraphNode {

    @Id
    @Property("templateNodeId")
    private Long templateNodeId;

    private String name;

    @Relationship(type = "RELATED_TO", direction = Relationship.Direction.OUTGOING)
    private Set<TemplateNodeRelation> relatedNodes = new HashSet<>();
}