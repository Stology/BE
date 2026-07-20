package com.stology.be.domain.node.entity.neo4j;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

@Node("Template")
@Getter
@NoArgsConstructor
public class TemplateGraphNode {

    @Id
    @GeneratedValue
    private Long templateId;

    private String title;

    @Relationship(type = "HAS_NODE", direction = Relationship.Direction.OUTGOING)
    private Set<TemplateNodeGraphNode> templateNodes = new HashSet<>();

    public TemplateGraphNode(String title) {
        this.title = title;
    }

    public void addTemplateNode(
            TemplateNodeGraphNode templateNode
    ) {
        this.templateNodes.add(templateNode);
    }
}