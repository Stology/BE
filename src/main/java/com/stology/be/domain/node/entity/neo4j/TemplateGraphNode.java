package com.stology.be.domain.node.entity.neo4j;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Template")
@Getter
@NoArgsConstructor
public class TemplateGraphNode {

    @Id
    @Property("templateId")
    private Long templateId;

    private String title;

    @Relationship(type = "HAS_NODE", direction = Relationship.Direction.OUTGOING)
    private Set<TemplateNodeGraphNode> templateNodes = new HashSet<>();
}